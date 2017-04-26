(ns n01se.hyperstruct
  (:require [n01se.deltype :as deltype])
  (:refer-clojure :exclude [map vector set list]))

"The goal of hyper data is to provide map/vector/list/set data structures that
support 'hyperlinks' as references. A hyperlink is a (relative) path to a place
somewhere in a hierarchy of data structures."

(alias 'clj 'clojure.core)

;; Type Definitions 

(defprotocol HyperLink
  (traverse- [_ root path]))

(defprotocol HyperColl
  (get-root- [_])
  (get-path- [_])
  (get-coll- [_]))

(defn hyper-assoc-in
  "safe assoc-in that works with hyper collections by recursively descending
  path until value is a assigned to the bottom. path must not include links."
  [hyper-coll path value]
  (if (empty? path)
    value
    (assoc hyper-coll (first path)
           (hyper-assoc-in (get (get-coll- hyper-coll) (first path) nil)
                           (rest path)
                           value))))

(defn hyper-get
  "Get the value associated with k from hyper-coll and traversing any links
  found to an appropriate value within the root context."
  [hyper-coll root path coll k d]
  (traverse-                              ; follow links via traverse-
    (get coll k d)                        ; get the raw value associated with k
    (hyper-assoc-in root path hyper-coll) ; update root with current hyper-coll
    (conj path k)))                       ; append k to the end of path

(deltype/deftype HyperList [root path coll i]
  :delegate [coll n01se.deltype.IList]

  n01se.deltype.IList
  (seq [self] self)

  (first [_]
    (traverse- (first coll)
               root
               (conj path i)))
  (next [_] (HyperList. root path (next coll) (inc i)))
  (pop [_] (HyperList. root path (pop coll) (inc i)))

  HyperLink

  (traverse- [_ new-root new-path]
    (HyperList. new-root new-path coll i))

  HyperColl

  (get-root- [_] root)
  (get-path- [_] path)
  (get-coll- [_] coll))

(deltype/deftype HyperMapCons [root path coll]
  :delegate [coll clojure.lang.ISeq]

  clojure.lang.ISeq
  (seq [self] self)
  (first [_] (traverse- (first coll) root path))

  HyperColl

  (get-root- [_] root)
  (get-path- [_] path)
  (get-coll- [_] coll))


(deltype/deftype HyperMapPair [root path coll]
  :delegate [coll clojure.lang.ISeq]

  clojure.lang.ISeq
  (seq [self] self)
  (first [_] (first coll))
  (next [_] (HyperMapCons. root (conj path (first coll)) (next coll)))

  HyperColl

  (get-root- [_] root)
  (get-path- [_] path)
  (get-coll- [_] coll))

(deltype/deftype HyperMapSeq [root path coll]
  :delegate [coll clojure.lang.ISeq]

  clojure.lang.ISeq

  (seq [self] self)
  (first [_] (HyperMapPair. root path (first coll)))
  (next [_] (HyperMapSeq. root path (next coll)))

  HyperColl

  (get-root- [_] root)
  (get-path- [_] path)
  (get-coll- [_] coll))

(deltype/deftype HyperMap [root path coll]
  :delegate [coll n01se.deltype.IEditableMap]

  n01se.deltype.IEditableMap
  (seq [self] (HyperMapSeq. (hyper-assoc-in root path self) path (seq coll)))
  (invoke [self k]   (hyper-get self root path coll k nil))
  (invoke [self k d] (hyper-get self root path coll k d))
  (valAt  [self k]   (hyper-get self root path coll k nil))
  (valAt  [self k d] (hyper-get self root path coll k d))

  HyperLink

  (traverse- [_ new-root new-path]
    (HyperMap. new-root new-path coll))

  HyperColl

  (get-root- [_] root)
  (get-path- [_] path)
  (get-coll- [_] coll))

(deltype/deftype HyperVectorSeq [root path coll i]
  :delegate [coll clojure.lang.ISeq]

  clojure.lang.ISeq
  (seq [self] self)
  (first [_] (traverse- (first coll) root (conj path i)))
  (next [_] (HyperVectorSeq. root path (next coll) (inc i)))

  HyperColl

  (get-root- [_] root)
  (get-path- [_] path)
  (get-coll- [_] coll))


(deltype/deftype HyperVector [root path coll]
  :delegate [coll n01se.deltype.IEditableVector]

  n01se.deltype.IEditableVector
  (seq [self] (HyperVectorSeq. (hyper-assoc-in root path self)
                               path
                               (seq coll)
                               0))

  (invoke [self k]   (hyper-get self root path coll k nil))
  (invoke [self k d] (hyper-get self root path coll k d))
  (valAt  [self k]   (hyper-get self root path coll k nil))
  (valAt  [self k d] (hyper-get self root path coll k d))
  (nth    [self k]   (hyper-get self root path coll k nil))
  (nth    [self k d] (hyper-get self root path coll k d))

  HyperLink

  (traverse- [_ new-root new-path]
    (HyperVector. new-root new-path coll))

  HyperColl

  (get-root- [_] root)
  (get-path- [_] path)
  (get-coll- [_] coll))

(deltype/deftype HyperSetSeq [root path coll]
  :delegate [coll clojure.lang.ISeq]

  clojure.lang.ISeq
  (seq [self] self)
  (first [_] (traverse- (first coll) root (conj path (first coll))))
  (next [_] (HyperSetSeq. root path (next coll)))

  HyperColl

  (get-root- [_] root)
  (get-path- [_] path)
  (get-coll- [_] coll))

(deltype/deftype HyperSet [root path coll]
  :delegate [coll n01se.deltype.ISet]

  n01se.deltype.ISet
  (seq [self] (HyperSetSeq. (hyper-assoc-in root path self) path (seq coll)))
  (get [self k]   (hyper-get self root path coll k nil))
  ;; Really... no get with defaults
  #_(get [self k]   (hyper-get self root path coll k d))

  HyperLink

  (traverse- [_ new-root new-path]
    (HyperSet. new-root new-path coll))

  HyperColl

  (get-root- [_] root)
  (get-path- [_] path)
  (get-coll- [_] coll))

(defn safe-get-in
  "Get the value associated with path from hyper-coll with explicit protection
  from circular links."
  [hyper-coll path]
  (let [visited (-> hyper-coll meta (get ::visited #{}))]
    (when-not (contains? visited path)
      (loop [c hyper-coll p (seq path)]
        (if (or (nil? c) (empty? p))
          c
          (recur (get (vary-meta c assoc ::visited (conj visited path))
                      (first p))
                 (next p)))))))

(defrecord Link [n link-path]
  HyperLink
  (traverse- [_ root path]
    (when (<= n (count path))
      (safe-get-in
        root
        (concat (drop-last n path) link-path)))))

(extend-protocol HyperLink
  nil ; nil objects traverse to nil
  (traverse- [_ root path] nil)
  Object ; non-hyper objects traverse to themselves
  (traverse- [obj root path] obj))

;; Printing
(defmethod print-method HyperList [h ^java.io.Writer w] (.write w (str (get-coll- h))))
(defmethod print-method HyperMapCons [h ^java.io.Writer w] (.write w (str (get-coll- h))))
(defmethod print-method HyperMapPair [h ^java.io.Writer w] (.write w (str (get-coll- h))))
(defmethod print-method HyperMapSeq [h ^java.io.Writer w] (.write w (str (get-coll- h))))
(defmethod print-method HyperMap [h ^java.io.Writer w] (.write w (str (get-coll- h))))
(defmethod print-method HyperVectorSeq [h ^java.io.Writer w] (.write w (str (get-coll- h))))
(defmethod print-method HyperVector [h ^java.io.Writer w] (.write w (str (get-coll- h))))
(defmethod print-method HyperSetSeq [h ^java.io.Writer w] (.write w (str (get-coll- h))))
(defmethod print-method HyperSet [h ^java.io.Writer w] (.write w (str (get-coll- h))))
(defmethod print-method Link [link ^java.io.Writer w]
    (.write w (str "(link " (:n link) " " (:link-path link) ")")))

;; API
(defn map [& x]
  (HyperMap. nil [] (apply clj/hash-map x)))

(defn vector [& x]
  (HyperVector. nil [] (apply clj/vector x)))

(defn list [& x]
  (HyperList. nil [] (apply clj/list x) 0))

(defn set [& x]
  (HyperSet. nil [] (clj/set x)))

(defn link [n path]
  (Link. n path))


