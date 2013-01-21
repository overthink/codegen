(ns codegen.generator
  "Generate a bunch of .class files from a simple data structure.  This code
  runs entirely at compile time and is not useful at runtime (though the
  classes it generates are theoretically useful at runtime)."
  (:require [clojure.walk :as walk]))

;; References:
;;
;; http://kotka.de/blog/2010/02/gen-class_how_it_works_and_how_to_use_it.html
;; http://clojure.github.com/clojure/clojure.core-api.html#clojure.core/gen-class
;; http://amalloy.hubpages.com/hub/Clojure-macro-writing-macros

(def target-ns "com.example.gen")

(defn schema
  "Returns a schema to use for class gen.  This is a fn just to demonstrate
  that the schema could be retrieved in any way: db call, api request, file on
  disk, etc."
  []
  [{:name :Address
    :props {:Street :String
            :City :String
            :Province :String}}
   {:name :Person
    :props {:firstName :String
            :lastName :String
            :age :Integer
            :address :Address}}])

;; Approach:
;; 1) Generate implementations for each class
;; 2) Compile these impls to .class files using gen-class

(defn gen-prop-impl
  "Generate an implementation for a single property of a class."
  [prefix pname ptype]
  (let [fn-name (symbol (name pname))]
    `(defn ~fn-name [~'this] (~'.state ~pname))))

(defn gen-class-impl
  "Return code that implements all the props and constructors for cls."
  [cls]
  (let [fq-class-name (str target-ns "." (name (:name cls)))
        prefix (str fq-class-name "-")
        props (:props cls)
        methods (vec
                  (for [[k v] props]
                    (let [fn-name (symbol (name k))
                          ;; Handle when return type is a generated type
                          return-type (if (contains? (set (map :name (schema))) v)
                                        (symbol (str target-ns "." (name v)))
                                        (symbol (name v)))]
                      [fn-name [] return-type])))]
    `(do
       ~@(for [[pname ptype] props]
           (gen-prop-impl prefix pname ptype))
       (gen-class
         :name ~fq-class-name
         :prefix ~prefix
         :state "state"
         :methods ~methods))))

(defmacro run
  "Generate implementations and gen-class calls for all classes described in
  schema."
  []
  `(do
     ~@(for [cls (schema)]
         (gen-class-impl cls))))
(comment
  (walk/macroexpand-all '(run))
  (macroexpand-1 '(run))
  )

(run)

(compile 'codegen.generator)

