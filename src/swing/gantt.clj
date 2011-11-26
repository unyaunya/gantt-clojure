(ns swing.gantt
  (:use util.calendar)
  (:import  (clojure.lang IPersistentVector)
            (org.jdesktop.swingx JXTreeTable)
            (swing.gantt.renderer)
            (swing.table SpanableTableHeader)
            (java.text SimpleDateFormat)
            (java.util Date Calendar GregorianCalendar)
            (javax.swing.table AbstractTableModel)
            (javax.swing.tree DefaultTreeCellRenderer)
            (org.jdesktop.swingx.treetable TreeTableModel AbstractTreeTableModel)
            ))

;------------------------------------------------------------------------------
; swing.gantt.GanttModel
;------------------------------------------------------------------------------
(gen-interface
  :name swing.gantt.GanttModel
  :extends [org.jdesktop.swingx.treetable.TreeTableModel]
  :methods [
      [getOrdinaryColumnCount [] int]
      [setDateSpan [java.util.Date java.util.Date] void]
      [getStartDate [] java.util.Date]
      [getEndDate [] java.util.Date]
      [getDate [int] java.util.Date]
    ]
)

;------------------------------------------------------------------------------
; swing.gantt.AbstractGanttModel
;------------------------------------------------------------------------------
(gen-class
  :name swing.gantt.AbstractGanttModel
  :extends org.jdesktop.swingx.treetable.AbstractTreeTableModel
  :implements [swing.gantt.GanttModel]
  :constructors {[clojure.lang.IPersistentVector
                  java.util.Date java.util.Date][]}
  :init   init
  :state  state
  :methods []
  :impl-ns swing.gantt
  :prefix "model-"
  )

(defn model-init[cNames start end]
  [[] (atom {:cnames cNames :start start :end end})])

(defn model-getOrdinaryColumnCount[this]
  (count (:cnames @(.state this))))

(defn model-setDateSpan[this start end]
  (reset! (.state this) assoc :start start :end end))
(defn model-getStartDate[this] (:start @(.state this)))
(defn model-getEndDate[this]   (:end @(.state this)))

(defn model-getDate[this columnIndex]
  (.getTime (c-add-day  (date->calendar (:start @(.state this)))
                        (- columnIndex (count (:cnames @(.state this)))))))

(defn model-getColumnCount[this] (+ 1
  (.getOrdinaryColumnCount this)
  (date-diff-day (.getEndDate this) (.getStartDate this))))

;------------------------------------------------------------------------------
; swing.gantt.DefaultTreeCellRenderer
;------------------------------------------------------------------------------
(comment
  (gen-class
    :name swing.gantt.DefaultTreeCellRenderer
    :extends javax.swing.tree.DefaultTreeCellRenderer
    :constructors {[swing.gantt.Gantt][]}
    :init   init
    :state  state
    :exposes-methods {paint paintSuper}
    :methods [
        [setVisibleRow [int] void]
        [getVisibleRow [] int]
      ]
    :impl-ns swing.gantt
    :prefix "tcr-"
    )

  (defn tcr-init[gantt] [[] {:treetable }])

  (defn tcr-setVisibleRow[this row])
  (defn tcr-paint[this g]
    (.paintSuper this g)
    (let [bounds  (cellRect)
          w       (dec (.width bounds))
          h       (dec (.height bounds))]
      ;(println bounds)
      (.setColor g c)
      (.drawLine g 0 h w h)
      (.drawLine g w 0 w h)))
)

;------------------------------------------------------------------------------
; swing.gantt.Gantt
;------------------------------------------------------------------------------
(gen-class
  :name swing.gantt.Gantt
  :extends org.jdesktop.swingx.JXTreeTable
  :constructors {[swing.gantt.GanttModel Object]
                 [org.jdesktop.swingx.treetable.TreeTableModel]}
  :init   init
  :post-init post-init
  :state  state
  :methods [[setDateSpan [java.util.Date java.util.Date] void]
            [getDate [int] java.util.Date]
            [getGanttModel [] swing.gantt.GanttModel]
            ]
  :exposes-methods {prepareRenderer prepareRendererSuper,
                    getCellRenderer getCellRendererSuper
                    setTreeTableModel setTreeTableModelSuper}
  :impl-ns swing.gantt
  :prefix "gantt-"
  )

(defn gantt-init
  " gantt-model: swing.gantt.GanttModel"
  [gantt-model cNames]
  [[gantt-model] {:cnames cNames :rendering-cell (atom [0 0])}])

(defn gantt-getGanttModel[this] (.getTreeTableModel this))
(defn gantt-setDateSpan[this start end]
      (.setDateSpan (.getGanttModel this) start end))
(defn gantt-getDate[this columnIndex]
      (.getDate (.getGanttModel this) columnIndex))
(defn gantt-getRenderingCell[this] @(:rendering-cell (.state this)))
(defn gantt-getRenderingRow[this] (get @(:rendering-cell (.state this)) 0))
(defn gantt-getRenderingRolumh[this] (get @(:rendering-cell (.state this)) 1))

(defn -create-header-model[gantt cNames]
  (let [header-value
          (fn[index]
            (let [start (.getStartDate (.getGanttModel gantt))]
              (if (< index 0) start
                (new Date (+ (.getTime start) (* index 86400000))))))
        c-start (count cNames)]
    (proxy [AbstractTableModel][]
      (getRowCount[] 3)
      (getColumnCount[] (+ 1 c-start (date-diff-day
                          (.getEndDate (.getGanttModel gantt))
                          (.getStartDate (.getGanttModel gantt)))))
      (getValueAt[row, col]
        (if (< col c-start) (get cNames col)
            (let [date (header-value (- col c-start))]
              (condp = row
                0 (year date)
                1 (inc (month date))
                2 (day-of-month date)
                "N/A")))))))

(defn gantt-header-values[h-model row col]
  (let [c-max (.getColumnCount h-model)]
    (concat [[col nil]]
            (for [i (range col c-max)]
              [i (.getValueAt h-model row i)])
            [[c-max nil]])))

(defn gantt-span[row v]
  (map (fn[[s e]] [row s 1 (- e s)])
    (partition 2 1
      (let [f (fn[[i1 v1] [i2 v2]] (when (not= v1 v2) i2))]
        (filter (fn[v]v) (for [[a b] (partition 2 1 v)] (f a b)))))))

(defn gantt-prepareTableHeader[this th]
  (if-let [state (.state this)]
    (let [g-model (.getGanttModel this)
          start   (.getStartDate g-model)
          end     (.getEndDate g-model)
          cNames  (:cnames state)
          h-model (-create-header-model this cNames)
          c-start (.getOrdinaryColumnCount g-model)
        ]
      (.addTableHeaderModel th h-model)
      (doseq [col (range c-start)]
        (.spanCells th 0 col 3 1))
      (doseq [row         (range 2)
              [r c rs cs] (gantt-span row
                            (gantt-header-values h-model row c-start))]
        (.spanCells th r c rs cs))
      th)))

(defn gantt-createDefaultTableHeader[this]
  (gantt-prepareTableHeader this (new SpanableTableHeader this)))

(defn gantt-post-init[this model cNames]
  (.setTableHeader this (.createDefaultTableHeader this))
  (.setTreeCellRenderer this
    (let [c (.getGridColor this)
          cellRect (fn[](let [cell (gantt-getRenderingCell this)]
                      (.getCellRect this (get cell 0) (get cell 1) false)))]
      (proxy[DefaultTreeCellRenderer][]
        (paint[g]
          (proxy-super paint g)
          (let [
                icon    (.getIcon this)
                text    (.getText this)
                xoffset (if (and icon text)
                            (+ (.getIconWidth icon)
                               (max 0 (.getIconTextGap this)) -1)
                            0)
                bounds  (cellRect)
                ;x1      (- 0 xoffset)
                x1      0
                y1      0
                w       (dec (.width bounds))
                h       (dec (.height bounds))
                x2 (+ x1 w) y2 (+ y1 h)
                ]
            (println xoffset)
            (println bounds)
            (.setColor g c)
            (.drawLine g x1 y1 x2 y2)
            (.drawLine g x1 y2 x2 y1)
            (.drawLine g x1 y2 x2 y2)
            (.drawLine g x2 y1 x2 y2)))))))

(defn gantt-prepareRenderer[this renderer, row, col]
  (reset! (:rendering-cell (.state this)) [row col])
  (let [c         (.prepareRendererSuper this renderer row col)
        value     (.getValueAt this row col)
        model-col (.convertColumnIndexToModel this col)]
    (if (.isRowSelected this row)
      (.setBackground c (.getSelectionBackground this))
      (.setBackground c (.getBackground this)))
    (if (>= model-col (.getOrdinaryColumnCount (.getGanttModel this)))
      (.setValue c {:work value :date (gantt-getDate this model-col)}))
    c))

(defn gantt-getCellRenderer[this row, col]
  (let [model-col (.convertColumnIndexToModel this col)]
    (cond
      (< model-col (count (:cnames (.state this))))
                (.getCellRendererSuper this row col)
      true      (new swing.gantt.renderer))))

(defn gantt-setTreeTableModel[this tt-model]
  (.setTreeTableModelSuper this tt-model)
  (gantt-prepareTableHeader this (.getTableHeader this)))

(defn treetablemodel-adapter[model start-date end-date]
  (proxy [swing.gantt.AbstractGanttModel]
          [["作業" "dummy" "開始日" "終了日"] start-date end-date]
    (getRoot[] (.getRoot model))
    (getChildCount[parent]   (.getChildCount model parent))
    (getChild[parent, index] (.getChild model parent index))
    (getValueAt[node, col]
      (if (< col (.getOrdinaryColumnCount this))
          (.getValueAt model node col)
          node))))
