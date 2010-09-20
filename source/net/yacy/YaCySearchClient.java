/**
 *  YaCySearchClient
 *  an interface for Adaptive Replacement Caches
 *  Copyright 2010 by Michael Peter Christen, mc@yacy.net, Frankfurt a. M., Germany
 *  First released 20.09.2010 at http://yacy.net
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *  
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program in the file lgpl21.txt
 *  If not, see <http://www.gnu.org/licenses/>.
 */

package net.yacy;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * most simple rss reader application for YaCy search result retrieval
 * this is an example application that you can use to integrate YaCy search results in your own java applications
 */
public class YaCySearchClient {

    /*
     * YaCy Search Results are produced in Opensearch format which is basically RSS.
     * The YaCy Search Result API Client is therefore implemented as a simple RSS reader.
     */
    private String host, query;
    private int port, offset;
    
    public YaCySearchClient(String host, int port, String query) {
        this.host = host; this.port = port; this.offset = -10; this.query = query;
    }
    
    public SearchResult next() throws IOException {
        this.offset += 10; // you may call this again and get the next results
        return new SearchResult();
    }
    
    public class SearchResult extends ArrayList<RSSEntry> {
        private static final long serialVersionUID = 1337L;
        public SearchResult() throws IOException {
            URL url;
            Document doc;
            String u =
                "http://" + host + ":" + port + "/yacysearch.rss?verify=false" +
                "&startRecord=" + offset + "&maximumRecords=10&resource=local" +
                "&query=" + query.replaceAll(" ", "+");
            try { url = new URL(u); } catch (MalformedURLException e) { throw new IOException (e); }
            try { doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openStream()); }
            catch (ParserConfigurationException e) { throw new IOException (e); }
            catch (SAXException e) { throw new IOException (e); }
            NodeList nodes = doc.getElementsByTagName("item");
            for (int i = 0; i < nodes.getLength(); i++)
                this.add(new RSSEntry((Element) nodes.item(i)));
        }
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (RSSEntry entry: this) sb.append(entry.toString());
            return sb.toString();
        }
    }

    public static class RSSEntry {
        String title, link;
        public RSSEntry(Element element) {
            title = val(element, "title", "");
            link  = val(element, "link", "");
        }
        private String val(Element parent, String label, String dflt) {
            Element e = (Element) parent.getElementsByTagName(label).item(0);
            Node child = e.getFirstChild();
            return (child instanceof CharacterData) ?
                    ((CharacterData) child).getData() : dflt;
        }
        public String toString() {
            return "Title : " + title + "\nLink  : " + link + "\n";
        }
    }
    
    /**
     * Call the main method with one argument, the query string
     * search results are then simply printed out.
     * Multiple search requests can be submitted by adding more call arguments.
     * Use this method as stub for an integration in your own programs
     */
    public static void main(String[] args) {
        for (String query: args) try {
            long t = System.currentTimeMillis();
            YaCySearchClient search = new YaCySearchClient("localhost", 8080, query);
            System.out.println("Search result for '" + query + "':");
            System.out.print(search.next().toString()); // get 10 results; you may repeat this for next 10
            System.out.println("Search Time: " + (System.currentTimeMillis() - t) + " milliseconds\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
