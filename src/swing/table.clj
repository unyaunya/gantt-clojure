(ns swing.table)

;------------------------------------------------------------------------------
; swing.table.SpanableTableHeaderUI
;------------------------------------------------------------------------------
(gen-class
  :name swing.table.SpanableTableHeaderUI
  :extends javax.swing.plaf.basic.BasicTableHeaderUI
  :exposes {rendererPane {:get rendererPane}
            header       {:get header}}
  :exposes-methods {getPreferredSize getPreferredSizeSuper}
  :impl-ns swing.table.span
  :prefix "ui-"
  )

;------------------------------------------------------------------------------
; swing.table.SpanableTableHeader
;------------------------------------------------------------------------------
(gen-class
  :name swing.table.SpanableTableHeader
  :extends javax.swing.table.JTableHeader
  :constructors {[javax.swing.JTable] [javax.swing.table.TableColumnModel]}
  :init   init
  :state  state
  :methods [[addTableHeaderModel [javax.swing.table.TableModel] void]
            [getTableHeaderModel [] javax.swing.table.TableModel]
            [setRowHeight [int] void]
            [getRowHeight [] int]
            [getColumnWidth [int] int]
            [spanCells [int int int int] void]
            [removeSpan [int int] void]
            [clearSpan [] void]
            [getCellRect [int int] java.awt.Rectangle]
          ]
  :post-init   post-init
  :exposes-methods {getPreferredSize getPreferredSizeSuper}
  :impl-ns swing.table.span
  :prefix "th-"
  )

;------------------------------------------------------------------------------
; swing.table.TreeTableModel
;------------------------------------------------------------------------------
(gen-interface
  :name swing.table.TreeTableModel
  :extends [javax.swing.table.TableModel]
  :methods [
      [getValue[Object, int] Object]
      [getChildCount [Object] int]
      [getChild[Object, int] Object]
      [isExpanded [int] boolean]
    ]
)

;------------------------------------------------------------------------------
; swing.table.AbstractTreeTableModel
;------------------------------------------------------------------------------
(gen-class
  :name swing.table.AbstractTreeTableModel
  :extends javax.swing.table.AbstractTableModel
  :implements [swing.table.TreeTableModel]
  :prefix "att-"
  :methods [
      [getRoot[] Object]
      [getNode [int] Object]
      [getRowCount [int] int]
      [getRowCount [Object] int]
  ]
  :impl-ns swing.table.group
)

;------------------------------------------------------------------------------
; swing.table.GroupColumnEditorRenderer
;------------------------------------------------------------------------------
(gen-class
  :name swing.table.GroupColumnEditorRenderer
  :extends javax.swing.AbstractCellEditor
  :implements [ javax.swing.table.TableCellRenderer
                javax.swing.table.TableCellEditor]
  :constructors {[swing.table.TreeTableModel] []}
  :init   init
  :state  state
  :methods []
  :post-init   post-init
  :impl-ns swing.table.group
  :load-impl-ns false
  :prefix "gcr-"
  )

;------------------------------------------------------------------------------
; swing.table.GroupableTable
;------------------------------------------------------------------------------
(gen-class
  :name swing.table.GroupableTable
  :extends javax.swing.JTable
  :constructors {[] [], [javax.swing.table.TableModel] []}
  :init   init
  :post-init post-init
  :state  state
  :methods []
  :exposes-methods {setModel setModelSuper}
  :impl-ns swing.table.group
  :prefix "gt-"
  )

