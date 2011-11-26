(ns swing.util
  (:import  (javax.swing JFrame JScrollPane)
            (javax.swing  JMenu JMenuItem JMenuBar JFileChooser
                          Action AbstractAction ImageIcon)
            (java.awt.event ActionListener)))

(defn action
  ([name, f, icon]
    (proxy[AbstractAction][name (if icon (new ImageIcon icon) nil)]
      (actionPerformed[e] (when f (f e)))))
  ([name, f] (action name f nil)))

(defn open-file-action
  ([name, open-fn] (open-file-action name open-fn nil))
  ([name, open-fn, icon]
    (let [ff (fn [e]
                (let [fc (new JFileChooser ".")
                      rc (.showOpenDialog fc (.getSource e))]
                  (when (= rc JFileChooser/APPROVE_OPTION)
                    (when open-fn (open-fn (.getSelectedFile fc))))))]
      (action name ff icon))))

(defn create-menu[name & items]
  (let [menu (JMenu. name)]
    (dorun
      (map #(.add menu (JMenuItem.
        (if (instance? Action %) % (action (first %)(second %))))) items))
    menu))

(defn create-menubar[menus]
  (let [menubar (JMenuBar.)]
    (dorun (map #(.add menubar (apply create-menu %)) menus))
    menubar))


