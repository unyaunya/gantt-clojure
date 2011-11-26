(ns swing.table.span
  (:import (java.awt Dimension Rectangle)))

;------------------------------------------------------------------------------
; swing.table.SpanableTableHeaderUI
;------------------------------------------------------------------------------
(defn ui-paint[this g c]
  (let [rp      (.rendererPane this) label (new javax.swing.JLabel)
        header  (.header this)
        cm      (.getColumnModel header)
        hm      (.getTableHeaderModel header)
        cb      (.getClipBounds g)
        paintCell (fn [rect row col]
                    (let [aColumn   (.getColumn cm col)
                          renderer  (.getHeaderRenderer aColumn)
                          renderer  (if (not renderer)
                                      (.getDefaultRenderer header))
                          component (.getTableCellRendererComponent
                                        renderer (.getTable header)
                                        (.getValueAt hm row col)
                                        false, false, -1, col)]
                         (.add rp component)
                         (.paintComponent rp g component header
                            (.x rect) (.y rect)
                            (.width rect) (.height rect) true)))
        ]
    (when cm
      (dorun (for [ row (range (.getRowCount hm))
                    col (range (.getColumnCount cm))]
                (let [rect (.getCellRect header row col)]
                  (when (and rect (.intersects rect cb))
                    (paintCell rect row col))))))))

(defn ui-getPreferredSize[this ignored]
  (let [dim     (.getPreferredSizeSuper this ignored)
        header  (.header this)
        model   (.getTableHeaderModel header)]
    (new Dimension (.width dim) (* (.getRowCount model) (.getRowHeight header)))))

;------------------------------------------------------------------------------
; swing.table.SpanableTableHeader
;------------------------------------------------------------------------------
(defn th-init[table]
  [[(.getColumnModel table)]
    { :model (atom nil)
      :row-height (atom 32)
      :span (atom {})
    }])
(defn th-post-init[this table]
  (.setUI this (new swing.table.SpanableTableHeaderUI)))

(defn th-setRowHeight [this h] (reset! (:row-height (.state this)) h))
(defn th-getRowHeight [this] @(:row-height (.state this)))
(defn th-getColumnWidth [this col]
  (.getWidth (.getColumn (.getColumnModel this) col)))

(defn th-spanCells [this startRow startColumn rowsToSpan columnsToSpan]
   (swap! (:span (.state this))
          assoc [startRow startColumn] [rowsToSpan columnsToSpan]))
(defn th-removeSpan [this startRow startColumn]
   (swap! (:span (.state this))
          dissoc [startRow startColumn]))
(defn th-clearSpan [this]
   (reset! (:span (.state this)) {}))

(defn -spanned?[span row col]
  (not (every? (fn [kv]
            (let [[[startRow startColumn] [rowsToSpan columnsToSpan]] kv
                  endRow (dec (+ startRow rowsToSpan))
                  endColumn (dec (+ startColumn columnsToSpan))]
              (or (< row startRow) (< endRow row)
                   (< col startColumn) (< endColumn col))))
          span)))

(defn th-getCellRect [this row col]
  (let [cm      (.getColumnModel this)
        margin  (when cm (.getColumnMargin cm))
        col-w   (fn[col] (.getWidth (.getColumn cm col)))
        colspan-w (fn[startColumn columnsToSpan]
                    (loop [x 0, i startColumn, n columnsToSpan]
                      (if (>= 0 n) x
                          (recur  (+ x (col-w i) margin)
                                  (inc i) (dec n)))))
        col-x   (fn[col]
                    (loop [i 0 x 0]
                      (if (>= i col) x
                          (recur (inc i) (+ x (col-w i) margin)))))
        row-h   (.getRowHeight this)
        row-y   (fn [row] (* row row-h))
        span    @(:span (.state this))
        si      (span [row col])]
        (cond
          si (let [[r-span c-span] si]
                (new Rectangle  (col-x col) (row-y row)
                                (colspan-w col c-span) (* r-span row-h)))
          (-spanned? span row col) nil
          true (new Rectangle (col-x col) (row-y row) (col-w col) row-h))))

(defn th-getReorderingAllowed
  "テーブルヘッダによるカラムの入替えを禁止する。"
  [table] false)

(defn th-addTableHeaderModel
  [this model] (reset! (:model (.state this)) model))
(defn th-getTableHeaderModel
  [this] @(:model (.state this)))
