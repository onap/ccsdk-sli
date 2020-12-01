This source repository contains the code for the CCSDK plugins.

To compile this code:

1. Make sure your local Maven settings file ($HOME/.m2/settings.xml) contains references to the ONAP repositories and OpenDaylight repositories.  See example-settings.xml for an example.

2. To compile, run "mvn clean install".


PropertyNode:
1) Takes any file then parses it and puts it to the context memory for Directed Graphs access.
2) Various parameters it takes:

    public String fileName; //Name of the file to put to properties

    public String contextPrefix; //Any prefix to add for your keys in the Properties context 

    public Set<String> listNameList;//only applies to XML based file parsing, you can use this to exclude a specific tree to be put to context. 

    public boolean fileBasedParsing;//enable to do a file based parsing, currently supports JSON and XML. 