(ns speccards.app
  (:require
    [devcards.core :as dc]
    [sablono.core :as sab :include-macros true]
    [cljs.spec :as s :include-macros true]
    [clojure.test.check.generators]
    [clojure.string :as str])
  (:require-macros
    [devcards.core :refer [defcard deftest]]))

(enable-console-print!)

(defn hidden [is-hidden]
  (if is-hidden
    {:display "none"}
    {}))

(defn pluralize [n word]
  (if (== n 1)
    word
    (str word "s")))

(s/def :todo/title (s/and string? (complement str/blank?)))
(s/def :todo/completed boolean?)
(s/def :todos/item (s/keys :req [:todo/title :todo/completed]))
(s/def :todos/list (s/coll-of :todos/item))
(s/def :todos/showing #{:all :active :completed})
(s/def :todos/view (s/keys :req [:todos/list :todos/showing]))

(defn item [{:keys [todo/title todo/completed todo/editing]}]
  (let [class (cond-> ""
                      completed (str "completed ")
                      editing (str "editing"))]
    (sab/html
      [:li {:className class}
       [:div.view
        [:input.toggle {:type     "checkbox"
                        :checked  (and completed "checked")
                        :onChange #(do %)}]
        [:label title]
        [:button.destroy]
        [:input.edit {:ref "editField"}]]])))

(defn todos [{:keys [todos/list todos/showing]}]
  (let [active (count (remove :todo/completed list))
        completed (- (count list) active)
        checked? (every? :todo/completed list)]
    (sab/html
      [:div#content
       [:div#todoapp
        [:header#header
         [:h1 "Todos"]
         [:input {:ref         "newField"
                  :id          "new-todo"
                  :placeholder "What needs to be done?"
                  :onKeyDown   #(do %)}]]
        [:section#main {:style (hidden (empty? list))}
         [:input#toggle-all {:type     "checkbox"
                             :onChange #(do %)
                             :checked  checked?}]
         (into [:ul#todo-list]
               (for [todo (filter (case showing
                                    :completed :todo/completed
                                    :active (complement :todo/completed)
                                    :all identity) list)]
                 (item todo)))]
        [:footer#footer {:style (hidden (empty? list))}
         [:span#todo-count
          [:strong active] (str " " (pluralize active "item") " left")]
         (into [:ul#filters {:className (name showing)}]
               (for [[x y] [["" "All"] ["active" "Active"] ["completed" "Completed"]]]
                 [:li [:a y]]))
         [:button#clear-completed (str "Clear completed (" completed ")")]]]])))

(doseq [ex (map first (s/exercise :todos/view))]
  (defcard todos-example
    (todos ex) ex {:inspect-data true}))

(defn init []
  (devcards.core/start-devcard-ui!))
