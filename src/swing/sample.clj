(ns swing.sample
  (:use   [swing.util])
  (:import  (javax.swing JFrame JScrollPane JTable)
            (javax.swing.table AbstractTableModel)
            (java.util Date)
            (java.text SimpleDateFormat)
            (swing.table GroupableTable AbstractTreeTableModel)
            ))

(def ^:dynamic *frame* (atom nil))
(def ^:dynamic *js-pane* (atom nil))
(def ^:dynamic *table* (atom nil))

(def *menu* [
  ["ファイル"
    ["閉じる" (fn[e](.setVisible @*frame* false))]
    ["印刷"  (fn[e]())]
    ["終了"  (fn[e](System/exit 0))]]
  ["編集"]])

(defstruct work :name :start :end :child)
(defn WORK [& vals]
  (let [[name start end child] vals
        start (Date. start)
        end   (Date. end)]
    (struct work name start end child)))

(def *data*
  (WORK "さんぷる・ぷろじぇくと" "2011/3/1" "2011/4/9" [
    (WORK "作業1" "2011/3/1" "2011/3/5" [
      (WORK "作業1-1", "2011/3/1" "2011/3/5")
      (WORK "作業1-2", "2011/3/1" "2011/3/7")
      (WORK "作業1-3", "2011/3/1" "2011/3/8")
    ])
    (WORK "作業2", "2011/3/1" "2011/3/7" [
      (WORK "作業2-1", "2011/3/1" "2011/3/5")
      (WORK "作業2-2", "2011/3/1" "2011/3/7" [
        (WORK "作業2-2-1", "2011/3/1" "2011/3/5")
        (WORK "作業2-2-2", "2011/3/1" "2011/3/7")
      ])
      (WORK "作業2-3", "2011/3/1" "2011/3/8")
    ])
    (WORK "作業3", "2011/3/1" "2011/3/8")
    (WORK "作業4", "2011/3/1" "2011/3/9")
  ]))

(def *calendar-date-format* (new SimpleDateFormat "MM/dd"))

(defn treetable-model[data]
  (let [cNames ["階層", "作業" "開始日" "終了日"]
        date2s #(.format *calendar-date-format* %)]
    (proxy [AbstractTreeTableModel][]
      (getRoot[] data)
      (getColumnCount[] (count cNames))
      (getColumnName[col] (get cNames col))
      (getChildCount[parent]   (count (:child parent)))
      (getChild[parent, index] (get (:child parent) index))
      (getValue[^Object node, col]
        (condp = col
          0 node
          1 (:name node)
          2 (date2s (:start node))""
          3 (date2s (:end node))))
      (isCellEditable[row, col] true)
          )))

(defn run[]
  (let [f (new JFrame)]
    (reset! *frame* f)
    (reset! *js-pane* (new JScrollPane
          JScrollPane/VERTICAL_SCROLLBAR_AS_NEEDED
          JScrollPane/HORIZONTAL_SCROLLBAR_AS_NEEDED))
    (reset! *table* (new GroupableTable))
    (.setModel @*table* (treetable-model *data*))
    (.setShowGrid @*table* true)
    (.setAutoResizeMode @*table* JTable/AUTO_RESIZE_OFF)
    (.setViewportView @*js-pane* @*table*)
    (.add f @*js-pane*)
    (.setJMenuBar f (create-menubar *menu*))
    (.setSize f 800 600)
    (.setTitle f "Table")
    (.setVisible f true)
    f))

