(ns conway-cljs.core
  (:require [reagent.core :as reagent :refer [atom]]))

(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload

(def width 10)
(def height 10)
(def item-count 3)

(def grid (reduce 
            #(let [[x y] %2]
               (assoc %1 [x y] (if (= x y)
                                 :alive
                                 :dead)))
            {}
            (for [x (range item-count)
                  y (range item-count)]
              [x y])))

(defonce state (atom {:grid grid
                      :width width
                      :height height
                      :item-count item-count}))

(defn get-neighbors [[x y]]
  (let [neighbors (for [xoff [-1 0 1]
                        yoff [-1 0 1]]
                    (apply vector (map + [x y] [xoff yoff])))
        neighbors-set (set neighbors)]
    (disj neighbors-set [x y])))

(defn hello-world []
  (let [{:keys [item-count
                height
                width
                grid]} @state]
    [:div {:style {:border "solid 1px black"
                   :display "inline-block"
                   :position "relative"
                   :flex-wrap "wrap"
                   :height (* item-count height)
                   :width (* item-count width)}}
     (doall
       (for [[coord status] grid]
         ^{:key coord} [:div {:style {:background-color (if (= :alive status)
                                                          "blue"
                                                          "white")
                                      :width width
                                      :height height
                                      :position "absolute"
                                      :top (* width (first coord))
                                      :left (* height (second coord))
                                      :display "inline-block"}
                              :on-click #(println (get-neighbors coord))}]))]))

(defn count-alives [grid neighbors]
  (reduce #(let [status (get grid %2)]
             (+ %1
                (case status
                  :alive 1
                  0)))
          0
          neighbors))

(defn update! [old-grid]
  (reduce 
    (fn [new-grid [coord status]]
      (let [neighbors (get-neighbors coord)
            alive-count (count-alives old-grid neighbors)]
        (println alive-count)
        (assoc new-grid coord (case alive-count
                                3 :alive
                                2 status
                                :dead))))
    {} ;; new grid to be returned
    grid))

(defn restart! [old-grid]
  grid)

(reagent/render-component [:div 
                           [hello-world]
                           [:div {:style {:position "relative"}} 
                            [:button {:on-click #(swap! state update :grid update!)} "update"]
                            [:button {:on-click (partial reset! state grid)} "Reset"]]]
                          (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )
