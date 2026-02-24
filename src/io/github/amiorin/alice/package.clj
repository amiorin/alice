(ns io.github.amiorin.alice.package
  (:require
   [babashka.process :as p]
   [big-config :as bc]
   [big-config.core :as core]
   [big-config.run :as run]
   [big-config.step-fns :as step-fns]
   [big-config.utils :refer [debug]]
   [big-config.workflow :as workflow]
   [cheshire.core :as json]
   [com.rpl.specter :as s]
   [io.github.amiorin.alice.tools :as tools]))

(def step-fns [workflow/print-step-fn
               (step-fns/->exit-step-fn ::end)
               (step-fns/->print-error-step-fn ::end)])

(defn extract-params
  [opts]
  (let [ip (-> (p/shell {:dir (workflow/path opts ::tools/tofu)
                         :out :string} "tofu show --json")
               :out
               (json/parse-string keyword)
               (->> (s/select-one [:values :root_module :resources s/FIRST :values :ipv4_address])))]
    {::workflow/params {:ip ip}}))

(def create
  (workflow/->workflow* {:first-step ::start-create
                         :last-step :end-create
                         :pipeline [::tools/tofu ["render tofu:init tofu:apply:-auto-approve"]
                                    ::tools/ansible ["render ansible-playbook:main.yml" extract-params]
                                    ::tools/ansible-local ["render ansible-playbook:main.yml" extract-params]]}))

(comment
  (debug tap-values
    (create [(fn [f step opts]
               (tap> [step opts])
               (f step opts))] {::bc/env :repl
                                ::run/shell-opts {:err *err*
                                                  :out *out*}
                                ::tools/tofu-opts (workflow/parse-args "render")
                                ::tools/ansible-opts (workflow/parse-args "render")
                                ::tools/ansible-local-opts (workflow/parse-args "render")}))
  (-> tap-values))

(def delete
  (workflow/->workflow* {:first-step ::start-delete
                         :last-step ::end-delete
                         :pipeline [::tools/tofu ["render tofu:init tofu:destroy:-auto-approve"]]}))

(defn alice
  [step-fns opts]
  (let [opts (merge {::workflow/create-fn create
                     ::workflow/delete-fn delete}
                    opts)
        wf (core/->workflow {:first-step ::start
                             :wire-fn (fn [step step-fns]
                                        (case step
                                          ::start [(partial workflow/run-steps step-fns) ::end]
                                          ::end [identity]))})]
    (wf step-fns opts)))

(comment
  (debug tap-values
    (alice [(fn [f step opts]
              (tap> [step opts])
              (f step opts))]
           {::bc/env :repl
            ::run/shell-opts {:err *err*
                              :out *out*}
            ::workflow/steps [:create]}))
  (-> tap-values))

(defn alice*
  [args & [opts]]
  (let [opts (merge (workflow/parse-args args)
                    {::bc/env :shell}
                    opts)]
    (alice step-fns opts)))

(comment
  (debug tap-values
    (alice* "delete create delete" {::bc/env :repl
                                    ::run/shell-opts {:err *err*
                                                      :out *out*}
                                    ::workflow/create-opts {::tools/tofu-opts {::workflow/steps [:render]}
                                                            ::tools/ansible-opts {::workflow/steps [:render]}
                                                            ::tools/ansible-local-opts {::workflow/steps [:render]}}
                                    ::workflow/delete-opts  {::tools/tofu-opts {::workflow/steps [:render]}}}))
  (-> tap-values))
