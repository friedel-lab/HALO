HALO - A Java framework for precise transcript half-life determination
===============

Software suite for the calculation of RNA half-lives from measurements
of newly transcribed, pre-existing and total RNA.

Website with data download: http://www.bio.ifi.lmu.de/software/halo



Getting started
---------------
After unpacking the download archive to a directory of your choice run

	gui.sh 

for the graphical user interface or

	cmdline.sh 
	
to start the command line tools

Use gui.bat and cmdline.bat if you are on Windows.

By default, the GUI and commandline tools use a maximum heap size of 1024MB.
If you need more than that, you can increase the max heap size using the -XmX
option in Java, e.g. :

	gui.sh -Xmx2000M
	cmdline.sh -Xmx2000M


Requirements
------------
HALO requires the Java 6.0 Runtime Environment or higher which is freely 
available at http://www.java.com

For visualization (optional) the JFreeChart library is used (available 
under the GNU Lesser General Public License at http://www.jfree.org/jfreechart/
and distributed with the HALO release).



Documentation
-------------
You find the documentation files in the doc/ subdirectory (start with index.html)
or online at http://www.bio.ifi.lmu.de/files/Software/halo/doc/index.html 


Package structure
-----------------

	*.sh        Shell startup scripts
	*.bat       Windows startup scripts
	README.txt  this file
	doc/        HALO documentation
	lib/        Compiled binaries
	src/        HALO source codes

Citation
-----------

Please cite the following publication if you use HALO in your work: Caroline C. Friedel, Stefanie Kaufmann, Lars Dölken, Ralf Zimmer, HALO - A Java framework for precise transcript half-life determination, Bioinformatics, vol. 26, no. 9, pp. 1264-1266, 2010. 
