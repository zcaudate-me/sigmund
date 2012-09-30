(ns sigmund.util
  (:require [clojure.string :as st]))


;; Human Readable Bytes

(def UNITS ["" "K" "M" "G" "T"])

(defn down-shift [num] (bit-shift-right num 10))

(defn up-shift [num] (bit-shift-left num 10))

(defn human-readable
  ([bytes] (human-readable bytes 0))
  ([bytes n]
     (loop [val bytes
            cnt 0]
       (let [nval  (down-shift val)
             nnval (first (drop n (iterate down-shift nval)))]
         (cond (zero? (or nnval nval)) (str val (nth UNITS cnt))
               :else (recur nval (inc cnt)))))))

;; Conversion into clojure objects

(defn lower-case [c]
  (str (.toLowerCase c)))

(defn- decamelcase [s]
  (let [ccs (st/replace s #"[-_]" "")   ;;  "__Say-Hi__" -> "SayHi"
        ccs (st/replace ccs #"^([A-Z])"   ;;  "SayHi"      -> "sayHi"
                        (fn [x] (lower-case (second x))))
        hs  (st/replace ccs #"[A-Z]"      ;;  "sayHi"      -> "say<c>hi"
                        (fn [x] (str "-" (lower-case x))))]
    hs))

(defmulti <clj (fn [obj] (type obj)))
(defmethod <clj nil [obj] obj)
(defmethod <clj Boolean [obj] obj)
(defmethod <clj String [obj] obj)
(defmethod <clj Number [obj] obj)
(defmethod <clj java.io.File [obj] obj)
(defmethod <clj java.util.Map [obj] (reduce (fn [m pair]
                                              (assoc m (first pair) (<clj (second pair))))
                                            {} (seq obj)))
(defmethod <clj java.util.List [obj] (map <clj obj))

(defmethod <clj Object [obj]
  (cond (.isArray (class obj)) (map <clj obj)

    :else
    (let [c (. obj (getClass))
          pmap (-> (reduce (fn [m ^java.beans.PropertyDescriptor pd]
                             (let [name (. pd (getName))
                                   method (. pd (getReadMethod))]
                               (if (and method (zero? (alength (. method (getParameterTypes)))))
                                 (assoc m
                                   (keyword (decamelcase name))
                                   (fn [] (clojure.lang.Reflector/prepRet
                                          (.getPropertyType pd) (. method (invoke obj nil)))))
                                 m)))
                           {}
                           (seq (.. java.beans.Introspector
                                    (getBeanInfo c)
                                    (getPropertyDescriptors))))
                   (dissoc :class))
          v (fn [k] ((pmap k)))
          snapshot (fn []
                     (reduce (fn [m e]
                               (assoc m (key e) ((val e))))
                             {} (seq pmap)))]
      (proxy [clojure.lang.APersistentMap]
          []
        (containsKey [k] (contains? pmap k))
        (entryAt [k] (when (contains? pmap k) (new clojure.lang.MapEntry k (v k))))
        (valAt ([k] (when (contains? pmap k) (v k)))
          ([k default] (if (contains? pmap k) (v k) default)))
        (cons [m] (conj (snapshot) m))
        (count [] (count pmap))
        (assoc [k v] (assoc (snapshot) k v))
        (without [k] (dissoc (snapshot) k))
        (seq [] ((fn thisfn [plseq]
                   (lazy-seq
                     (when-let [pseq (seq plseq)]
                       (cons (new clojure.lang.MapEntry (first pseq) (v (first pseq)))
                             (thisfn (rest pseq)))))) (keys pmap)))))))

(defn de-get [m-str]
  (decamelcase (st/replace m-str #"^get" "")))

(defn de-is [m-str]
  (str (decamelcase (st/replace m-str #"^is" "")) "?"))

(defn de-java [m-str]
  (cond (re-find #"^get[A-Z]" m-str) (de-get m-str)
        (re-find #"^is[A-Z]" m-str) (de-is m-str)
        :else (decamelcase m-str)))
