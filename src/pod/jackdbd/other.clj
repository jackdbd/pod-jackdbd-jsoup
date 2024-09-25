(ns pod.jackdbd.other)

(defn multi-arity-func
  ([]
   (multi-arity-func 1 1))
  ([n]
   (multi-arity-func n 1))
  ([n k]
   (* n k)))

(comment
  (multi-arity-func)
  (multi-arity-func 7)
  (multi-arity-func 7 3))