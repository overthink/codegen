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

(defn fq
  "Fully qualify x with target-ns. e.g. 'Foo' -> 'com.example.gen.Foo'.  Works
  with strings, symbols, and keywords."
  [x]
  (str target-ns "." (name x)))

(defn schema
  "Returns a schema to use for class gen.  This is a fn just to demonstrate
  that the schema could be retrieved in any way: db call, api request, file on
  disk, etc."
  []
  [{:name :Address
    :props {:street :String
            :city :String
            :province :String}}
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
    `(defn ~fn-name [this#] (~'.state this# ~pname))))

(defn gen-class-impl
  "Return code that implements all the props and constructors for cls."
  [cls]
  (let [fq-class-name (fq (:name cls))
        prefix (str fq-class-name "-")
        props (:props cls)
        ;; Handle when return type is a generated type
        type-symbol (fn [type-name]
                      (if (contains? (set (map :name (schema))) type-name)
                                        (symbol (fq (name type-name)))
                                        (symbol (name type-name))))
        ;; Just one constructor to which a value for each prop is passed
        ctor-types (->> props (vals) (map type-symbol))
        init-fn (symbol (str fq-class-name "-init"))
        methods (vec
                  (for [[k v] props]
                    (let [fn-name (symbol (name k))
                          return-type (type-symbol v)]
                      [fn-name [] return-type])))]
    `(do
       ;; First generate the impls of each property
       ~@(for [[pname ptype] props]
           (gen-prop-impl prefix pname ptype))
       ;; Next generate the init/ctor function, returns value for state
       (defn ~init-fn [& ~'ctor-args]
         [[] (zipmap ~(keys props) ~'ctor-args)])
       ;; Then emit the gen-class call to save .class files when compiled
       (gen-class
         :name ~fq-class-name
         :prefix ~prefix
         :state "state"
         :init ~init-fn
         :constructors {~ctor-types []}
         :methods ~methods))))

(defmacro gen-classes
  "Generate implementations and gen-class calls for all classes described in
  schema."
  []
  `(do
     ~@(for [cls (schema)]
         (gen-class-impl cls))))

;; Generate all the classes and write .class files.
(gen-classes)

(comment
  (walk/macroexpand-all '(gen-classes))
  (pprint (macroexpand-1 '(gen-classes)))
)

