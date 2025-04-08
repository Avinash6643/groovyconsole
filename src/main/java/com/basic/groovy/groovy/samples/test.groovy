package com.basic.groovy.groovy.samples

Calendar calendar = Calendar.getInstance()
List<String> dateList = new ArrayList<>()
dateList.add(calendar.get(Calendar.YEAR).toString())
dateList.add((calendar.get(Calendar.MONTH) + 1).toString())

println(dateList)