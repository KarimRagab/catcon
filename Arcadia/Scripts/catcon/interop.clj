(ns catcon.interop)

(defmacro set-with! [obj [sym prop] & body]
  `(let [obj# ~obj
         ~sym (. obj# ~prop)]
     (set! (. obj# ~prop) (do ~@body))))

(defmacro condcast-> [expr xsym & clauses]
  (let [exprsym (gensym "exprsym_")
        [clauses default] (if (even? (count clauses))
                            [clauses nil] 
                            [(butlast clauses)
                             [:else
                              `(let [~xsym ~exprsym]
                                 ~(last clauses))]])
        cs (->> clauses
             (partition 2)
             (mapcat
               (fn [[t then]]
                 `[(instance? ~t ~exprsym)
                   (let [~(with-meta xsym {:tag t}) ~exprsym]
                     ~then)])))]
    `(let [~exprsym ~expr]
       ~(cons 'cond
          (concat cs default)))))