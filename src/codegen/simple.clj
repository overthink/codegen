(ns codegen.simple
  "Playing around with generating classes for use from non-Clojure.")

(defn com.example.simple.Foo-init [& args]
  #_(println (format "init args: %s" args))
  ;; Return [ superclass-ctor-args value-to-use-for-state ]
  [[] {:first (nth args 0)
       :second (nth args 1)
       :third (nth args 2)}])

(defmacro getter 
  "Define a simple 'getter' fn for the given keyword."
  [x]
  `(defn ~(symbol (format "com.example.simple.Foo-%s" (name x))) [this#]
    (~(keyword x) (.state this#))))

(getter :first)
(getter :second)
(getter :third)

(gen-class
  :name "com.example.simple.Foo"
  :prefix "com.example.simple.Foo-"
  :state "state"
  :init "init"
  :constructors {[String String Integer] []}
  :methods [["first" [] String]
            ["second" [] String]
            ["third" [] Integer]])

(compile 'codegen.simple)

(def f (com.example.simple.Foo. "a" "b" (int 42)))
(.state f)
(.first f)
(.second f)
(.third f)

