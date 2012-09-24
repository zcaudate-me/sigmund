(ns sigmund.util
  (:require [clojure.string :as st]))

(defn lower-case [c]
  (str (.toLowerCase c)))

(defn- decamelcase [s]
  (let [ccs (st/replace s #"[-_]" "")   ;;  "__Say-Hi__" -> "SayHi"
        ccs (st/replace ccs #"^([A-Z])"   ;;  "SayHi"      -> "sayHi"
                        (fn [x] (lower-case (second x))))
        hs  (st/replace ccs #"[A-Z]"      ;;  "sayHi"      -> "say<c>hi"
                        (fn [x] (str "-" (lower-case x))))]
    hs))

(defn map->hashmap [m]
  (assoc (into {} m) "key" "value")

  )



(defmulti <clj (fn [obj] (type obj)))
(defmethod <clj String [obj] obj)
(defmethod <clj Number [obj] obj)
(defmethod <clj java.io.File [obj] obj)
(defmethod <clj java.util.Map [obj] (into {} obj))
(defmethod <clj java.util.List [obj] (seq obj))
(defmethod <clj Object [obj]
  (let [c (. obj (getClass))
        pmap (reduce (fn [m ^java.beans.PropertyDescriptor pd]
                         (let [name (. pd (getName))
                               method (. pd (getReadMethod))]
                           (if (and method (zero? (alength (. method (getParameterTypes)))))
                             (assoc m (keyword (decamelcase name)) (fn [] (clojure.lang.Reflector/prepRet (.getPropertyType pd) (. method (invoke obj nil)))))
                             m)))
                     {}
                     (seq (.. java.beans.Introspector
                              (getBeanInfo c)
                              (getPropertyDescriptors))))
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
                           (thisfn (rest pseq)))))) (keys pmap))))))
