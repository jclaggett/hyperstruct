(ns n01se.hyperstruct.examples
  (:require [n01se.hyperstruct :as h]
            [clojure.pprint :refer [pprint]]))

(comment 1 "Context: representing complex data structures" 
  (def list-struct [a b c d])
  (def tree-struct [a [b c] d])
  (def dag-struct [a [b "ref to 1st item"] d]) 
  (def graph-struct [a [b "ref to 3rd item"] [c "ref to 2nd item"]]))

; # mutable python examples
; dag = [a [b] c]
; dag[1].append(dag[0])
;
; graph = [a [b] [c]]
; graph[1].append(graph[2])
; graph[2].append(graph[1])

(comment 2 "Introducing hyperlinks"
  (def link-to-self (h/link 0 []))
  (def link-to-parent (h/link 1 []))
  (def link-to-sibling (h/link 1 [:sibling-name]))

  (def list-struct [a b c d])
  (def tree-struct [a [b c] d])
  (def dag-struct [a [b (h/link 2 [0])] d]) 
  (def graph-struct [a [b (h/link 2 [2] [c (h/link 2 [1])])]]))

(comment 3 "Introducing hyperstructs"
  (def ex3 (h/map :a 42
                  :b (h/link 1 [:a])
                  :c (h/map :sub-a 43
                            :super-a (h/link 2 [:b]))
                  :d (h/link 1 [:c :sub-a])))

  (get ex3 :a)               ;=> 42
  (get ex3 :b)               ;=> also 42
  (get-in ex1 [:c :super-a]) ;=> also, also 42
  (get ex3 :d)               ;=> 43
  
  (def family (h/map :bob (h/map :sister (h/link 2 [:sue])
                                 :secret 42)
                     :sue (h/map :brother (h/link 2 [:bob])
                                 :secret nil)))
  (defn wisper [family]
    (assoc-in family [:bob :sister :secret]
              (get-in family [:bob :secret]))))
         
;; Stress testing
(def x
  (h/map
    :. (h/link 1 [])
    :a 1
    :b 2

    ;; collections
    :sub-vec (h/vector 2 3 (h/link 2 [:a]))
    :sub-set (h/set 1 2 3 4) 
    :sub-list (h/list 1 2 3 4 (h/link 2 []))

    ;; various links
    :c (h/link 1 [:a])
    :d (h/link 1 [:c])
    :e (h/link 1 [:. :. :sub-vec])
    :f (h/link 1 [:sub-vec 2])
    :g (h/link 1 [:e 2])

    ;; circular links
    :h (h/link 1 [:i])
    :i (h/link 1 [:j])
    :j (h/link 1 [:h])))
    

;; Scottland yard board data copied from:
;; http://github.com/abrooks/scotlandyard
(def raw-board
  {1 {:taxi #{8 9} :bus #{58 46} :train #{46}}
   2 {:taxi #{10 20}}
   3 {:taxi #{4 11 12} :bus #{22 23}}
   4 {:taxi #{3 13}}
   5 {:taxi #{15 16}}
   6 {:taxi #{7 29}}
   7 {:taxi #{6 17} :bus #{42}}
   8 {:taxi #{1 18 19}}
   9 {:taxi #{1 19 20}}
   10 {:taxi #{2 11 21 34}}
   11 {:taxi #{3 10 22}}
   12 {:taxi #{3 23}}
   13 {:taxi #{4 23 24} :bus #{14 23 52} :train #{46 67 89}}
   14 {:taxi #{15 25} :bus #{13 15}}
   15 {:taxi #{5 14 16 26 28} :bus #{14 29 41}}
   16 {:taxi #{5 15 28 29}}
   17 {:taxi #{7 29 30}}
   18 {:taxi #{8 31 43}}
   19 {:taxi #{8 9 32}}
   20 {:taxi #{2 9 33}}
   21 {:taxi #{10 33}}
   22 {:taxi #{11 23 34 35} :bus #{3 23 34 65}}
   23 {:taxi #{12 13 22 37} :bus #{3 13 22 67}}
   24 {:taxi #{13 37 38}}
   25 {:taxi #{14 38 39}}
   26 {:taxi #{15 27 39}}
   27 {:taxi #{26 28 40}}
   28 {:taxi #{15 16 27 41}}
   29 {:taxi #{6 16 17 41 42} :bus #{15 41 42 55}}
   30 {:taxi #{17 42}}
   31 {:taxi #{18 43 44}}
   32 {:taxi #{19 33 44 45}}
   33 {:taxi #{20 21 32 46}}
   34 {:taxi #{10 22 47 48} :bus #{22 46 63}}
   35 {:taxi #{22 36 48 65}}
   36 {:taxi #{35 37 49}}
   37 {:taxi #{23 24 36 50}}
   38 {:taxi #{24 25 50 51}}
   39 {:taxi #{25 26 51 52}}
   40 {:taxi #{27 41 52 53}}
   41 {:taxi #{28 29 40 54} :bus #{15 29 52 87}}
   42 {:taxi #{29 30 56 72} :bus #{7 29 72}}
   43 {:taxi #{18 31 57}}
   44 {:taxi #{31 32 58}}
   45 {:taxi #{32 46 58 59 60}}
   46 {:taxi #{33 45 47 61} :bus #{1 58 34 78} :train #{1 13 74 79}}
   47 {:taxi #{34 46 62}}
   48 {:taxi #{34 35 62 63}}
   49 {:taxi #{36 50 66}}
   50 {:taxi #{37 38 49}}
   51 {:taxi #{38 39 52 67 68}}
   52 {:taxi #{39 40 51 69} :bus #{13 41 67 86}}
   53 {:taxi #{40 54 69}}
   54 {:taxi #{41 53 55 70}}
   55 {:taxi #{54 71} :bus #{29 89}}
   56 {:taxi #{42 91}}
   57 {:taxi #{43 58 73}}
   58 {:taxi #{44 45 57 59 74 75} :bus #{1 46 74 77}}
   59 {:taxi #{45 58 75 76}}
   60 {:taxi #{45 61 76}}
   61 {:taxi #{46 60 62 76 78}}
   62 {:taxi #{47 48 61 79}}
   63 {:taxi #{48 64 79 80} :bus #{34 65 79 100}}
   64 {:taxi #{63 65 81}}
   65 {:taxi #{35 64 66 82} :bus #{22 63 67 82}}
   66 {:taxi #{49 65 67 82}}
   67 {:taxi #{51 66 68 84} :bus #{23 52 65 82 102} :train #{13 79 89 111}}
   68 {:taxi #{51 67 69 85}}
   69 {:taxi #{52 53 68 86}}
   70 {:taxi #{54 71 87}}
   71 {:taxi #{55 70 72 89}}
   72 {:taxi #{42 71 90 91} :bus #{42 105 107}}
   73 {:taxi #{57 74 92}}
   74 {:taxi #{58 73 75 92} :bus #{58 94} :train #{46}}
   75 {:taxi #{58 59 74 94}}
   76 {:taxi #{59 60 61 77}}
   77 {:taxi #{76 78 95 96} :bus #{58 78 94 124}}
   78 {:taxi #{61 77 79 97} :bus #{46 77 79}}
   79 {:taxi #{62 63 78 98} :bus #{63 78} :train #{46 93 67 111}}
   80 {:taxi #{63 99 100}}
   81 {:taxi #{64 82 100}}
   82 {:taxi #{65 66 81 101} :bus #{65 67 100 140}}
   83 {:taxi #{101 102}}
   84 {:taxi #{67 85}}
   85 {:taxi #{68 84 103}}
   86 {:taxi #{69 103 104} :bus #{52 87 102 116}}
   87 {:taxi #{70 88} :bus #{41 86 105}}
   88 {:taxi #{87 89 117}}
   89 {:taxi #{71 88 105} :bus #{55 105} :train #{13 67 140 128}}
   90 {:taxi #{72 91 105}}
   91 {:taxi #{56 72 90 105 107}}
   92 {:taxi #{73 74 93}}
   93 {:taxi #{92 94} :bus #{94} :train #{79}}
   94 {:taxi #{75 93 95} :bus #{74 77 93}}
   95 {:taxi #{77 94 122}}
   96 {:taxi #{77 97 109}}
   97 {:taxi #{78 96 98 109}}
   98 {:taxi #{79 97 99 110}}
   99 {:taxi #{80 98 110 112}}
   100 {:taxi #{80 81 101 112 113} :bus #{63 82 111}}
   101 {:taxi #{82 83 100 114}}
   102 {:taxi #{83 103 115} :bus #{67 86 127}}
   103 {:taxi #{85 86 102}}
   104 {:taxi #{86 116}}
   105 {:taxi #{89 90 91 106 108} :bus #{72 87 89 107 108}}
   106 {:taxi #{105 107}}
   107 {:taxi #{91 106 119} :bus #{72 105 161}}
   108 {:taxi #{105 117 119} :bus #{105 116 135} :boat #{115}}
   109 {:taxi #{96 97 110 124}}
   110 {:taxi #{98 99 109 111}}
   111 {:taxi #{110 112 124} :bus #{100 124} :train #{67 79 153 163}}
   112 {:taxi #{99 100 111 125}}
   113 {:taxi #{100 114 125}}
   114 {:taxi #{101 113 115 126 131 132}}
   115 {:taxi #{102 114 126 127} :boat #{108 157}}
   116 {:taxi #{104 117 118 127} :bus #{86 108 127 142}}
   117 {:taxi #{88 108 116 129}}
   118 {:taxi #{116 129 134 142}}
   119 {:taxi #{107 108 136}}
   120 {:taxi #{121 144}}
   121 {:taxi #{120 122 145}}
   122 {:taxi #{95 121 123 146} :bus #{123 144}}
   123 {:taxi #{122 124 137 148 149} :bus #{122 124 144 165}}
   124 {:taxi #{109 111 123 130 138} :bus #{77 111 123 153}}
   125 {:taxi #{112 113 131}}
   126 {:taxi #{114 115 127 140}}
   127 {:taxi #{115 116 126 133 134} :bus #{102 116 133}}
   128 {:taxi #{142 143 160 172 188} :bus #{135 142 161 187 199} :train #{89 140 185}}
   129 {:taxi #{117 118 135 142 143}}
   130 {:taxi #{124 131 139}}
   131 {:taxi #{114 125 130}}
   132 {:taxi #{114 140}}
   133 {:taxi #{127 140 141} :bus #{127 140 157}}
   134 {:taxi #{118 127 141 142}}
   135 {:taxi #{129 136 143 161} :bus #{108 128 161}}
   136 {:taxi #{119 135 162}}
   137 {:taxi #{123 147}}
   138 {:taxi #{124 150 152}}
   139 {:taxi #{130 140 153 154}}
   140 {:taxi #{126 132 133 139 154 156} :bus #{82 133 154 156} :train #{89 128 153}}
   141 {:taxi #{133 134 142 158}}
   142 {:taxi #{118 129 134 141 143 158 128} :bus #{116 128 157}}
   143 {:taxi #{128 129 135 142 160}}
   144 {:taxi #{120 145 177} :bus #{122 123 163}}
   145 {:taxi #{121 144 146}}
   146 {:taxi #{122 145 147 163}}
   147 {:taxi #{137 146 164}}
   148 {:taxi #{123 149 164}}
   149 {:taxi #{123 148 150 165}}
   150 {:taxi #{138 149 151}}
   151 {:taxi #{150 152 165 166}}
   152 {:taxi #{138 151 153}}
   153 {:taxi #{139 152 154 166 167} :bus #{124 154 180 184} :train #{111 140 163 185}}
   154 {:taxi #{139 140 153 155} :bus #{140 153 156}}
   155 {:taxi #{154 156 167 168}}
   156 {:taxi #{140 155 157 169} :bus #{140 154 157 184}}
   157 {:taxi #{156 158 170} :bus #{133 142 156 185} :boat #{115 194}}
   158 {:taxi #{141 142 157 159}}
   159 {:taxi #{158 170 172 186 198}}
   160 {:taxi #{128 143 161 173}}
   161 {:taxi #{135 160 174} :bus #{107 128 135 199}}
   162 {:taxi #{136 175}}
   163 {:taxi #{146 177} :bus #{144 176 191} :train #{111 153}}
   164 {:taxi #{147 148 178 179}}
   165 {:taxi #{149 151 179 180} :bus #{123 180 191}}
   166 {:taxi #{151 153 181 183}}
   167 {:taxi #{153 155 168 183}}
   168 {:taxi #{155 167 184}}
   169 {:taxi #{156 184}}
   170 {:taxi #{157 159 185}}
   171 {:taxi #{173 175 199}}
   172 {:taxi #{128 159 187}}
   173 {:taxi #{160 171 174 188}}
   174 {:taxi #{161 173 175}}
   175 {:taxi #{162 171 174}}
   176 {:taxi #{177 189} :bus #{163 190}}
   177 {:taxi #{144 163 176}}
   178 {:taxi #{164 189 191}}
   179 {:taxi #{164 165 191}}
   180 {:taxi #{165 181 193} :bus #{165 153 184 190}}
   181 {:taxi #{166 180 182 193}}
   182 {:taxi #{181 183 195}}
   183 {:taxi #{166 167 182 196}}
   184 {:taxi #{168 169 185 196 197} :bus #{153 156 180 185}}
   185 {:taxi #{170 184 186} :bus #{157 184 187} :train #{128 153}}
   186 {:taxi #{159 185 198}}
   187 {:taxi #{172 188 198} :bus #{128 185}}
   188 {:taxi #{128 173 187 199}}
   189 {:taxi #{176 178 190}}
   190 {:taxi #{189 191 192} :bus #{176 180 191}}
   191 {:taxi #{178 179 190 192} :bus #{163 165 190}}
   192 {:taxi #{190 191 194}}
   193 {:taxi #{180 181 194}}
   194 {:taxi #{192 193 195} :boat #{157}}
   195 {:taxi #{182 194 197}}
   196 {:taxi #{183 184 197}}
   197 {:taxi #{184 195 196}}
   198 {:taxi #{159 186 187 199}}
   199 {:taxi #{171 188 198} :bus #{128 161}}})

;; Working with explicit references
(defn find-shortest-paths [board origin goal]
  (loop [i 1
         visited #{}
         candidates {origin #{[origin]}}]

    (println (str "Iteration " i ": "
                  (count candidates) " candidates, "
                  (count visited) " visited"))

    (if (empty? candidates)
      #{} ;; Give up with no paths found

      (if (contains? candidates goal)
        (get candidates goal) ;; Found our goal so get its paths

        (let [visited (into visited (keys candidates))
              candidates (reduce
                           (fn [m [mode station path]]
                             (update-in m [station]
                                        (fnil conj #{})
                                        (conj path mode station)))
                           {}
                           (for [[candidate paths] candidates
                                 [mode stations] (get board candidate)
                                 station stations
                                 :when (not (contains? visited station))
                                 path paths]
                             [mode station path]))]
          (recur (inc i) visited candidates))))))

;; Board defined with hyperlinks

(defn transform-board [board-data]
  (into
    (h/vector)
    (for [[_ station] (into (sorted-map) board-data)]
      (into
        (h/map)
        (for [[mode dests] station]
          [mode (into
                  (h/set)
                  (for [dest dests]
                    (h/link 3 [(dec dest)])))])))))

(def board (transform-board raw-board))
