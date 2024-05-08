import groovy.io.FileType
import java.io.File
import groovy.xml.*

def tomcatHome = "C:\\apache-tomcat-10.1.23\\apache-tomcat-10.1.23"
def keystoreFile = new File(tomcatHome, "conf\\sample.keystore")

println 'Edit server.xml file...'
def xmlFile = new File(tomcatHome, '\\conf\\server.xml')
//def serverXml = new XmlSlurper().parseText(xmlFile)
//println serverXml
def parser = new XmlParser()
def serverXml = parser.parse(xmlFile)

println serverXml.Service[0].Connector[0]
println '-----'

def thisConnector = serverXml.Service[0].Connector[0]
thisConnector.@keystoreFile = "${keystoreFile}"
thisConnector.@keystorePass = "mypassword"
thisConnector.@keystoreType = "JKS"

println serverXml.Service[0].Connector[0]

// Note: This does not preserve any connects in the xml file that was read in, modified, and then read out
new XmlNodePrinter(new PrintWriter(new FileWriter(xmlFile))).print(serverXml)