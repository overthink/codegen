(ns codegen.generator)

(def target-ns "com.example.gen")

(def classes
  [[:Address {:Street :String
              :City :String
              :Province :String}]
   [:Person {:firstName :String
             :lastName :String
             :age :Int
             :address :Address}]])

(defmacro gen
  [class-sym props]
  (let [class-name (name class-sym)
        prefix (str class-name "-")
        fq-class-name (str target-ns "." class-name)
        init-fn-name (str class-name "-init")]
    `(do
       ;;~props
       (gen-class
         :name ~(symbol class-name)
         :prefix ~(symbol prefix)
         :state ~(symbol "state") ;; bareword 'state' ends up resolved in current ns
         :init ~(symbol init-fn-name)))))

(macroexpand-1 `(gen Address {Street String}))

;;(generate test-schema)
;;(compile 'codegen.generator)

