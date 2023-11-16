#
# compile all java files and wrap them in the appropriate jar files
#
# If you want to run this in eclipse you need to remove all JAR files from directory

JAVAC = /usr/bin/javac
JAR = jar 
RECTDIR = vasco/rectangles
REGIONDIR = vasco/regions
POINTDIR = vasco/points
LINEDIR = vasco/lines
COMMONDIR = vasco/common
DRAWABLEDIR = vasco/drawable
CLASSDIR = classes
DATE = `date +'%b %d, %Y'`
# INSTALLDIR = /fs/sandjava/www/htdocs/quadtree


all:  rectangles.jar points.jar lines.jar regions.jar

clean:
	-rm *.jar
	-rm -R $(CLASSDIR)/*
	-rm */*~

regions.jar: FORCE
	-sed -e s/XXXXX/"$(DATE)"/ $(COMMONDIR)/compiledate.pseudojava > $(COMMONDIR)/CompileDate.java
	$(JAVAC) -d $(CLASSDIR) $(REGIONDIR)/*.java
	$(JAVAC) -d $(CLASSDIR) $(DRAWABLEDIR)/*.java
	/bin/cp -f $(COMMONDIR)/*.gif $(CLASSDIR)/$(COMMONDIR)
	$(JAR) -cvf regions.jar -C $(CLASSDIR) $(REGIONDIR) -C  $(CLASSDIR) $(COMMONDIR) -C  $(CLASSDIR) $(DRAWABLEDIR)

rectangles.jar: FORCE
	-sed -e s/XXXXX/"$(DATE)"/ $(COMMONDIR)/compiledate.pseudojava > $(COMMONDIR)/CompileDate.java
	$(JAVAC) -d $(CLASSDIR) $(RECTDIR)/*.java
	$(JAVAC) -d $(CLASSDIR) $(DRAWABLEDIR)/*.java
	$(JAVAC) -d $(CLASSDIR) $(COMMONDIR)/*.java
	/bin/cp -f $(COMMONDIR)/*.gif $(CLASSDIR)/$(COMMONDIR)
	$(JAR) -cvf rectangles.jar -C $(CLASSDIR) $(RECTDIR) -C  $(CLASSDIR) $(COMMONDIR) -C  $(CLASSDIR) $(DRAWABLEDIR)

lines.jar: FORCE
	-sed -e s/XXXXX/"$(DATE)"/ $(COMMONDIR)/compiledate.pseudojava > $(COMMONDIR)/CompileDate.java
	$(JAVAC) -d $(CLASSDIR) $(LINEDIR)/*.java
	$(JAVAC) -d $(CLASSDIR) $(DRAWABLEDIR)/*.java
	/bin/cp -f $(COMMONDIR)/*.gif $(CLASSDIR)/$(COMMONDIR)
	$(JAR) -cvf lines.jar -C $(CLASSDIR) $(LINEDIR) -C $(CLASSDIR) $(COMMONDIR) -C $(CLASSDIR) $(DRAWABLEDIR)

points.jar: FORCE
	-sed -e s/XXXXX/"$(DATE)"/ $(COMMONDIR)/compiledate.pseudojava > $(COMMONDIR)/CompileDate.java
	$(JAVAC) -d $(CLASSDIR) $(POINTDIR)/*.java
	$(JAVAC) -d $(CLASSDIR) $(POINTDIR)/randy/*.java
	$(JAVAC) -d $(CLASSDIR) $(DRAWABLEDIR)/*.java
	$(JAVAC) -d $(CLASSDIR) $(COMMONDIR)/*.java
	$(JAVAC) -d $(CLASSDIR) $(RECTDIR)/*.java
	$(JAVAC) -d $(CLASSDIR) $(REGIONDIR)/*.java
	$(JAVAC) -d $(CLASSDIR) $(LINEDIR)/*.java
	/bin/cp -f $(COMMONDIR)/*.gif $(CLASSDIR)/$(COMMONDIR)
	$(JAR) -cvf points.jar -C $(CLASSDIR) $(POINTDIR) -C $(CLASSDIR) $(LINEDIR) -C $(CLASSDIR) $(REGIONDIR) -C $(CLASSDIR) $(COMMONDIR) -C $(CLASSDIR) $(DRAWABLEDIR) java.policy.applet


# install:
# 	/bin/cp -f points.jar $(INSTALLDIR)/points/
# 	/bin/cp -f rectangles.jar $(INSTALLDIR)/rectangles/
# 	/bin/cp -f lines.jar $(INSTALLDIR)/lines/
# 	/bin/cp -f regions.jar $(INSTALLDIR)/regions/

FORCE:


