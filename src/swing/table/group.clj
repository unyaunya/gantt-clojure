(ns swing.table.group
  (:import  (javax.swing JPanel JCheckBox JLabel JButton JToggleButton ImageIcon)
            (javax.swing.table DefaultTableModel)))

;------------------------------------------------------------------------------
; swing.table.AbstractTreeTableModel
;------------------------------------------------------------------------------
(defn att-isExpanded [this row] true)

(defn att-getRowCount
  ([this] (dec (.getRowCount this (.getRoot this))))
  ([this node]
    (reduce + 1 (map #(.getRowCount this (.getChild this node %))
                (range (.getChildCount this node))))))

(defn att-getNode[this row]
  (loop [r        row
         node     (.getRoot this)
         index    0
         c-count  (.getChildCount this node)]
    (when (< index c-count)
      (let [c_i (.getChild this node index)]
        (if (= r 0) c_i
          (let [rows_i (.getRowCount this c_i)]
            (if (<= rows_i r)
              (recur (- r rows_i)
                     node
                     (inc index)
                     c-count)
              (recur (dec r)
                     c_i
                     0
                     (.getChildCount this c_i)))))))))

(defn att-getValueAt[this row, col]
  (.getValue this (.getNode this row) col))

;------------------------------------------------------------------------------
; swing.table.GroupColumnEditorRenderer
;   TreeTable用のCellRenderer,CellEditor
;------------------------------------------------------------------------------
(defn get-icon[resource-name]
  (ImageIcon. (.getResource
    (java.lang.ClassLoader/getSystemClassLoader) resource-name)))

(def plus-icon (get-icon "swing/img/plus_button.png"))
(def minus-icon (get-icon "swing/img/minus_button.png"))
(def empty-icon (get-icon "swing/img/empty.png"))

(defn gcr-renderer[]
  (println "gcr-renderer3")
  (let [panel (new JPanel)
        label (new JLabel)
        button (new JLabel)
        ]
    (.setIcon button plus-icon)
    (.setText label "N/A")
    (.setLayout panel nil)
    (.add panel button)
    (.add panel label)
    (.setLocation button 0 0)
    (.setBounds button 0 0  (.getIconWidth  (.getIcon button))
                          (.getIconHeight (.getIcon button)))
    (.setBounds label (.width (.getSize button)) 0 200 32)
    ;(.addActionListener )
    (println (.getBounds button))
    (println (.getBounds label))
    panel))

(defn gcr-init[model] [[] {:model model :component (gcr-renderer)}])
(defn gcr-post-init[this model] nil)
(defn gcr-getTableCellRendererComponent
      [this table value isSelected hasFocus row column]
  (if (:child value) (:component (.state this)) nil))
(defn gcr-getTableCellEditorComponent
      [this table value isSelected row column]
  (if (:child value) (:component (.state this)) nil))
(defn gcr-getCellEditorValue[this] true)

;------------------------------------------------------------------------------
; swing.table.GroupableTable
;------------------------------------------------------------------------------
(defn gt-init
  ([] (gt-init nil))
  ([model]
    (let [gce (new swing.table.GroupColumnEditorRenderer model)]
      [[] (atom {:original-model model :gce gce :expanded {}})] )))
(defn gt-post-init
  ([this] (gt-post-init this (new DefaultTableModel)))
  ([this model]
    (.setRowHeight this 32)
    ;(.setModel this model)
    ))
(defn gt-getTableCellRenderer[table]
  (if-let [state (.state table)] (:gce @state)))
(defn gt-model-adapter[this model])
(defn gt-setModel[this model]
  "第１カラムのCellRenderer,CellEditorを、TreeTable用のものにセットする。"
  (.setModelSuper this model)
  (let [cm  (.getColumnModel this)
        tc0 (when (> (.getColumnCount cm) 0) (.getColumn cm 0))]
    (when tc0
      (.setCellRenderer tc0 (gt-getTableCellRenderer this))
      (.setCellEditor tc0 (gt-getTableCellRenderer this)))))

