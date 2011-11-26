(ns gantt.model
  (:use   [clojure.contrib.json :only (read-json pprint-json)]
          [clojure.contrib.duck-streams :only (reader writer)]
          [swing.gantt]
          )
  (:import  (java.util Date Calendar GregorianCalendar)
            (org.jdesktop.swingx.treetable AbstractTreeTableModel)
            (java.text SimpleDateFormat)
            (javax.swing.table AbstractTableModel)
            ))

(require '(org.danlarkin [json :as json]))

;日付フォーマット
(def *save-date-format* (new SimpleDateFormat "yyyy/MM/dd"))
(def *calendar-date-format* (new SimpleDateFormat "MM/dd"))

;----------------------------
(defstruct work :name :start :end :child)
(defn WORK [& vals]
  (let [[name start end child] vals
        start (Date. start)
        end   (Date. end)]
    (struct work name start end child)))

(def data-atom (atom 
  (WORK "さんぷる・ぷろじぇくと" "2011/3/1" "2011/4/9" [
    (WORK "作業1" "2011/3/1" "2011/3/5" [
      (WORK "作業1-1", "2011/3/1" "2011/3/5")
      (WORK "作業1-2", "2011/3/1" "2011/3/7")
      (WORK "作業1-3", "2011/3/1" "2011/3/8")
    ])
    (WORK "作業2", "2011/3/1" "2011/3/7")
    (WORK "作業3", "2011/3/1" "2011/3/8")
    (WORK "作業4", "2011/3/1" "2011/3/9")
  ])))

(defn get-data[] @data-atom)
(defn set-data[value]
  (reset! data-atom value))

;TreeTableModel
(defn treetable-model[data]
  (let [cNames ["作業" "dummy" "開始日" "終了日"]
        date2s #(.format *calendar-date-format* %)]
    (proxy [AbstractTreeTableModel][]
      (getRoot[] data)
      (getColumnCount[] (count cNames))
      (getColumnName[col] (get cNames col))
      (getChildCount[parent]   (count (:child parent)))
      (getChild[parent, index] (get (:child parent) index))
      (getValueAt[node, col]
        (condp = col
          0 (:name node)
          1 ""
          2 (date2s (:start node))""
          3 (date2s (:end node)))))))

(defn fs-model[]
  (let [data (get-data)]
    (treetablemodel-adapter (treetable-model data)
                            (:start data) (:end data))))

;ファイル入出力
(defn -convert[conv-fn wk]
  (let [start (conv-fn (:start wk))
        end   (conv-fn (:end   wk))
        child (vec (for [i (:child wk)] (-convert conv-fn i)))]
      (assoc wk :start start :end end :child child)))

(defn save-data[file-name]
  (let [date->str #(if (= Date (class %))
                       (.format *save-date-format* %) %)]
    (with-open [w (writer file-name)]
      (json/encode-to-writer (-convert date->str (get-data)) w :indent 4))))

(defn read-data[file-name]
  (let [str->date #(if (= String (class %))
                       (.parse *save-date-format* %) %)]
    (with-open [r (reader file-name)]
      (-convert str->date (json/decode-from-reader r)))))
