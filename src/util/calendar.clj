(ns util.calendar
  (:import  (java.text SimpleDateFormat)
            (java.util Date Calendar GregorianCalendar)))

;------------------------------------------------------------------------------
;Calendar/Date操作
;------------------------------------------------------------------------------
(defn date->calendar[date] (let [cal (Calendar/getInstance)]
                              (.setTime cal date) cal))

(defmacro year[date]  `(.get (date->calendar ~date) GregorianCalendar/YEAR))
(defmacro month[date] `(.get (date->calendar ~date) GregorianCalendar/MONTH))
(defmacro day[date]   `(.get (date->calendar ~date) GregorianCalendar/DAY_OF_YEAR))
(defmacro day-of-month[date] `(.get (date->calendar ~date) GregorianCalendar/DAY_OF_MONTH))

(defmacro msec[date]  `(.getTime ~date))

(defn date-diff-month[d1 d2]
  (- (+ (year d1) (month d1)) (+ (year d2) (month d2))))
(defn date-diff-day[d1 d2]
  (- (quot (msec d1) 86400000) (quot (msec d2) 86400000)))

(defn c-add-day[c days]
  (let [cl (.clone c)]
    (.add cl Calendar/DAY_OF_YEAR days)
    cl))
