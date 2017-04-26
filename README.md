# n01se.hyperstruct

A Clojure library designed to describe 'hyper links' usable inside otherwise
normal immutable data structures. These links are similar in concept to either
symbolic links in Unix file systems or to the hyperlinks found in html pages.

## Usage

```clojure
(require '[n01se.hyperstruct :as h])

(def sample (h/map :a 42
                   :b (h/link 1 [:a])
                   :c (h/map :sub-a 43
                             :super-a (h/link 2 [:a]))
                   :d (h/link 1 [:c :sub-a]))

(get sample :a)               ;=> 42
(get sample :b)               ;=> also 42
(get-in sample [:c :super-a]) ;=> also, also 42
(get sample :d)               ;=> 43
```


## Clojure Meetup Outline and notes

1. Some Context: a brief comparison between immutable and mutable data.
  1. Both immutable and mutable data structures can describe trees
  2. Mutable data structures may have can also describe DAGs and circular
     references.
  3. In contrast, immutable data can not describe DAGs (or circular refs)
  4. The only option available for immutable data is to store keys (and paths)
     into the overall data structure as references.
    1. Note: I think this option is great. The rest of this discussion is
       optional so long as you remember the single point that immutable data
       must represent DAGs via paths into the data structure. 
  5.  Question: Can we make these immutable references more convenient and/or
      more 'self evident' in the data?

2. Introducing hyperlinks: h/link
  1. The central concept of `n01se.hyperstruct` is a hyperlink value which
     'points' to a place in the data structure my describing a how to get
     there from the link's place in the data structure.
  2. Specifically, a hyperlink, as defined by this library, is: `(h/link n path)`
     where: `n` is the number of levels up the data structure from the link's
     position and `path` is a list of keys in the data structure down to a
     position.
    1. Note: all links are relative since their is no absolute 'root' of a data
       structure.

3. Introducing hyperstructs: h/list, h/vector, h/map, h/set
  1. Hyperstructs are just like Clojure's standard data structures with the
     additional feature of following 'hyperlinks'.
  2. Even though a hyperlink is just a value, attempting to get a hyperlink from a
     hyperstruct will actually cause the hyperstruct to get the value
     referenced by the hyperlink.
  3. Ground rules of hyperlinks in hyperstructs:
    1. link following will only be done through hyperstructs
    2. links referenced by links will also be followed
    3. self-referencing links (directly or indirectly) will return nil
    4. following a link to a non-existent path will return nil
  4. Question: What is returned from wisper?

4. Questions (I can go into implementation if interest)

## License

Copyright © 2017 Jonathan Claggett

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
