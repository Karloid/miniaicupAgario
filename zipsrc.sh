#!/bin/bash
cd ./src/main/java; zip -r -X "../../../lastSources.zip" * -x LibGdxPainter.java -x LibGdxObj.java -x LibGdxShower.java -x LibGdxDataToPaint.java
