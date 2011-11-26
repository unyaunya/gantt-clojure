(ns swing.gantt.renderer
  ;(use gantt.model)
  (:import  (javax.swing JLabel)
            (javax.swing.table DefaultTableCellRenderer)
            (java.awt Color)
            (java.text SimpleDateFormat)
            (java.util Date Calendar GregorianCalendar)
  )
  (:gen-class :extends javax.swing.table.DefaultTableCellRenderer
    :init init
    :state state
    :exposes-methods  { paintComponent paintComponentSuper}
    :methods [[getValue [] Object]]
  )
)

(defn -init [] [[] (atom {})])
(defn -setValue[this value] (reset! (.state this) value))
(defn -getValue[this] @(.state this))

(defn in?[work date]
  (let [s (.getTime (:start work))
        e (.getTime (:end work))
        d (.getTime date)]
    (and (<= s d) (<= d e))))

(defn -paintComponent[this g]
  ;(.paintComponentSuper this g)
  (let [value (.getValue this)
        work  (:work value)
        date  (:date value)
        h (.getHeight this)]
    (if (and value work date)
        (if (in? work date)
          (.fillRect g 0 (* 0.4 h) (.getWidth this) (* 0.2 h))))))

