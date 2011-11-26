(ns gantt.core
  (:use   [gantt.model]
          [swing.util]
          [swing.gantt])
  (:import  (javax.swing JFrame JScrollPane JTable)
            (java.util Date)
            (swing.gantt Gantt)))

(def *frame* (atom nil))
(def *js-pane* (atom nil))
(def *gantt* (atom nil))

(defn refresh-model[]
  (.setTreeTableModel @*gantt* (fs-model)))

(def *menu* [
  ["ファイル"
    (open-file-action "新規" #(println (.getName %)))
    (open-file-action "開く" #(do (set-data (read-data (.getName %)))
                                  (refresh-model)))
    (open-file-action "保存" #(save-data (.getName %)))
    ["閉じる" (fn[e](.setVisible @*frame* false))]
    ["印刷"  (fn[e]())]
    ["終了"  (fn[e](System/exit 0))]]
  ["編集"]])

(defn run[]
  (let [f (new JFrame)]
    (reset! *frame* f)
    (reset! *js-pane* (new JScrollPane
          JScrollPane/VERTICAL_SCROLLBAR_AS_NEEDED
          JScrollPane/HORIZONTAL_SCROLLBAR_AS_NEEDED))
    (reset! *gantt* (new Gantt (fs-model) ["作業" "" "開始日" "終了日"]))
    (.setShowGrid @*gantt* true)
    (.setAutoResizeMode @*gantt* JTable/AUTO_RESIZE_OFF)
    (.setViewportView @*js-pane* @*gantt*)
    (.add f @*js-pane*)
    (.setJMenuBar f (create-menubar *menu*))
    (.setSize f 800 600)
    (.setTitle f "Gantt")
    (.setVisible f true)
    f))

