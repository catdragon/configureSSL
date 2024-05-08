import groovy.io.FileType
import java.io.File
import groovy.xml.XmlSlurper
import groovy.xml.XmlParser

// TODO: Ultimately we would want to create a UI interface to gather the info set below/used to execute the keytool application.
//   I am making the assumption tha this will be converted into a Java/Groovy application in some IDE
def tomcatHome = "C:\\apache-tomcat-10.1.23\\apache-tomcat-10.1.23"
def keystoreFile = new File(tomcatHome, "conf\\sample.keystore")
def truststoreFile = new File(tomcatHome, "conf\\sample.truststore")
def javaPath = System.getenv('JAVA_HOME')

if (!keystoreFile.exists()) {
    // Create a keystore file if it does not exist.
    println 'Creating keystore...'
    def firstCmd = """
$javaPath\\bin\\keytool -genkey -noprompt -keyalg RSA \
-alias tomcat -dname "CN=sas.com, OU=ID, OU=SAS, L=Cary, S=NC, C=US" \
-keystore $keystoreFile -storepass changeit -keypass changeit
"""
    println "Executing ${firstCmd}"
    def sout = new StringBuffer()
    def serr = new StringBuffer()
  
    // consumeProcessOutput() gets the output and error streams from a process 
    // and reads them to keep the process from blocking due to a full output 
    // buffer. The processed stream data is appended to the supplied 
    // OutputStream. For this, two Threads are started, so this method will 
    // return immediately. The threads will not be join()ed, even if waitFor() 
    // is called. To wait for the output to be fully consumed call 
    // waitForProcessOutput().
    // TODO: At the current time, attempting the following produces an error of "Unknown password type:"  Yet
    //   when I run it on the command line, it works.  I am unsure why it doesn't work like this, but moving 
    //   on so i am not bogged down here.
    def process = firstCmd.execute()
    process.consumeProcessOutput(sout, serr)
    process.waitForProcessOutput()
    println 'Result ---'
    System.out << sout.toString()
    System.out << serr.toString()
    println 'end result ---'
} else {
    println 'Keystore exists.'
}

// Edit the Tomcat configuration file to add the SSL/TLS settings.
// note that the port may be different based on the needs of the setup
println 'Edit server.xml file...'
def xmlFile = new File(tomcatHome, '\\conf\\server.xml')
def serverXml = new XmlParser().parse(xmlFile)

def thisConnector = serverXml.Service[0].Connector[0]
//can change the port in the following line
thisConnector.@port = 443
thisConnector.@keystoreFile = "${keystoreFile}"
// TODO: fillin the correct password from the user's inputs
thisConnector.@keystorePass = "mypassword"
thisConnector.@keystoreType = "JKS"

// Note: This does not preserve any connects in the xml file that was read in, modified, and then read out
// TODO: write the new server.xml file to a differently named xml file, perhaps one that the user of the app can specify
new XmlNodePrinter(new PrintWriter(new FileWriter(xmlFile))).print(serverXml)

//TODO: restart the Tomcat server
