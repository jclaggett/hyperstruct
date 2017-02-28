(ns n01se.hyperstruct.core
  (:require [n01se.deltype :as deltype]))

"The goal of hyper data is to provide map/vector/list/set data structures that
support 'hyperlinks' as references. A hyperlink is a (relative) path to a place
somewhere in a hierarchy of data structures."

;; Type Definitions 
(defprotocol HyperLink
  (traverse- [_ root path]))

(defprotocol HyperColl
  (get-root- [_])
  (get-path- [_])
  (get-coll- [_]))

(defn hyper-assoc-in
  "assoc-in that works with hyper collections by recursively descending path
  until value is a assigned to the bottom."
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
  (traverse-                              ; call follow links via traverse-
    (get coll k d)                        ; get the raw value associated with k
    (hyper-assoc-in root path hyper-coll) ; update root with current hyper-coll
    (conj path k)))                       ; append k to the end of path

(deltype/deftype HyperMap [root path coll]
  :debug true
  :delegate [coll n01se.deltype.IEditableMap]
  
  n01se.deltype.IEditableMap

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

(deltype/deftype HyperVector [root path coll]
  :debug true
  :delegate [coll n01se.deltype.IEditableVector]
  
  n01se.deltype.IEditableVector

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

(defrecord Link [n link-path]
  HyperLink
  (traverse- [_ root path]
    (when (<= n (count path))
      (get-in root (concat (drop-last n path) link-path)))))

(extend-protocol HyperLink
  nil ; nil objects traverse to nil
  (traverse- [_ root path] nil)
  Object ; non-hyper objects traverse to themselves
  (traverse- [obj root path] obj))

;; API
(defn hypermap [x]
  (HyperMap. nil [] x))

(defn hypervector [x]
  (HyperVector. nil [] x))

(defn hyperlist [x]
  (HyperList. nil [] x))

(defn link [n path]
  (Link. n path))

(defmethod print-method Link [link ^java.io.Writer w]
    (.write w (str "(link " (:n link) " " (:link-path link) ")")))

(def x
  (hypermap {:a 1
             :b (hypervector [2
                              3
                              (link 2 [:a])])
             :c (link 1 [:a])
             :d (link 1 [:c])
             :e (link 1 [:b])
             :f (link 1 [:b 2])
             :g (link 1 [:e 2])}))

