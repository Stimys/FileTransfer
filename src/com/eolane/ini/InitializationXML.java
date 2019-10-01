package com.eolane.ini;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InitializationXML {
    private final String XML_LINE_FILE_NAME = System.getProperty("user.dir")+ File.separator+"Config"+File.separator+"LibraryTransferIni.xml";
    private File xmlInput = new File(XML_LINE_FILE_NAME);
    private SAXParserFactory factory;
    private SAXParser saxParser;

    private boolean bSourceDirection;
    private boolean bProgramFolder;
    private boolean bTransferDirection;
    private boolean pathFound;

    private String sourceDirection;
    private String programFolder;
    private String transferDirection;

    private HashMap<String, List<File>> directions;

    private List<File> pathList;

    public HashMap<String, List<File>> parse(){
        bSourceDirection = false;
        bProgramFolder = false;
        bTransferDirection = false;

        pathList = new ArrayList<>();
        directions = new HashMap<>();

        factory = SAXParserFactory.newInstance();

        try{
            saxParser = factory.newSAXParser();

            DefaultHandler handler = new DefaultHandler() {
            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

                if (qName.equalsIgnoreCase("SourceDirection")) {
                    bSourceDirection = true;
                } else if (qName.equalsIgnoreCase("ProgramFolder")) {
                    bProgramFolder = true;
                } else if (qName.equalsIgnoreCase("TransferDirection")) {
                    bTransferDirection = true;
                } else if (qName.equalsIgnoreCase("path")) {
                    pathFound = true;
                }


            }

                @Override
                public void endElement (String uri, String localName, String qName)
                        throws SAXException
                {
                    if (qName.equalsIgnoreCase("SourceDirection")) {
                        bSourceDirection = false;
                        directions.put("SourceDirection", new ArrayList<>(pathList));
                        pathList.clear();
                    } else if (qName.equalsIgnoreCase("ProgramFolder")) {
                        bProgramFolder = false;
                        directions.put("ProgramFolder", new ArrayList<>(pathList));
                        pathList.clear();
                    } else if (qName.equalsIgnoreCase("TransferDirection")) {
                        bTransferDirection = false;
                        directions.put("TransferDirection", new ArrayList<>(pathList));
                        pathList.clear();
                    }
                }

            @Override
            public void characters(char ch[], int start, int length) throws SAXException {

                if(pathFound){
                    if (bSourceDirection) {
                        sourceDirection = new String(ch, start, length);
                        pathList.add(new File(sourceDirection));
                        pathFound = false;
                    } else if (bProgramFolder) {
                        programFolder = new String(ch, start, length);
                        pathFound = false;
                        pathList.add(new File(programFolder));
                    } else if (bTransferDirection) {
                        transferDirection =  new String(ch, start, length);
                        pathFound = false;
                        pathList.add(new File(transferDirection));
                    }
                }
            }
        };

            try{
                saxParser.parse(xmlInput, handler);
            } catch (IOException exp){
                exp.printStackTrace();
            }

        } catch (SAXException | ParserConfigurationException exp){
            exp.printStackTrace();
        }


        return  directions;
    }
}
