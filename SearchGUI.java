package luc;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Font;
import java.awt.TextArea;

import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.UIManager;
import javax.swing.JTextArea;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.ar.ArabicAnalyzer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class SearchGUI extends JFrame {

	private JPanel contentPane;
	private JTextField textField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SearchGUI frame = new SearchGUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	
	public static void doPagingSearch(BufferedReader in, IndexSearcher searcher, Query query, 
            int hitsPerPage, boolean raw, boolean interactive) throws IOException {

// Collect enough docs to show 5 pages
TopDocs results = searcher.search(query, 1000 * hitsPerPage);//Set number of hits per page,1000 is the maximum. ,This can be set at indexing time, as a default. And can be overridden at query time
ScoreDoc[] hits = results.scoreDocs;

int numTotalHits = Math.toIntExact(results.totalHits);
System.out.println(numTotalHits + " total matching documents");

int start = 0;
int end = Math.min(numTotalHits, hitsPerPage);

while (true) {
if (end > hits.length) {
System.out.println("Only results 1 - " + hits.length +" of " + numTotalHits + " total matching documents collected.");
System.out.println("Collect more (y/n) ?");
String line = in.readLine();
if (line.length() == 0 || line.charAt(0) == 'n') {
break;
}

hits = searcher.search(query, numTotalHits).scoreDocs;
}

end = Math.min(hits.length, start + hitsPerPage);

for (int i = start; i < end; i++) {
if (raw) {                              // output raw format
System.out.println("doc="+hits[i].doc+" score="+hits[i].score);
continue;
}

Document doc = searcher.doc(hits[i].doc);
String path = doc.get("path");
if (path != null) {
System.out.println((i+1) + ". " + path);
//TextArea.append((i+1) + "." + path);
String title = doc.get("title");
if (title != null) {
System.out.println("   Title: " + doc.get("title"));
//TextArea.append("Title" + doc.get("title"));

}
} else {
System.out.println((i+1) + ". " + "No path for this document");
//textArea.append((i+1) + "." + "No path for this document");

}

}

if (!interactive || end == 0) {
break;
}

if (numTotalHits >= end) {
boolean quit = false;
while (true) {
System.out.print("Press ");
if (start - hitsPerPage >= 0) {
System.out.print("(p)revious page, ");  
}
if (start + hitsPerPage < numTotalHits) {
System.out.print("(n)ext page, ");
}
System.out.println("(q)uit or enter number to jump to a page.");

String line = in.readLine();
if (line.length() == 0 || line.charAt(0)=='q') {
quit = true;
break;
}
if (line.charAt(0) == 'p') {
start = Math.max(0, start - hitsPerPage);
break;
} else if (line.charAt(0) == 'n') {
if (start + hitsPerPage < numTotalHits) {
start+=hitsPerPage;
}
break;
} else {
int page = Integer.parseInt(line);
if ((page - 1) * hitsPerPage < numTotalHits) {
start = (page - 1) * hitsPerPage;
break;
} else {
System.out.println("No such page");
}
}
}
if (quit) break;
end = Math.min(numTotalHits, start + hitsPerPage);
}
}
}

	
	public SearchGUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblQuery = new JLabel("Query:");
		lblQuery.setFont(new Font("Arial Black", Font.BOLD, 16));
		lblQuery.setForeground(new Color(165, 42, 42));
		lblQuery.setBounds(25, 16, 69, 33);
		contentPane.add(lblQuery);
		
		textField = new JTextField();
		textField.setBounds(104, 15, 271, 40);
		contentPane.add(textField);
		textField.setColumns(10);
		
		JButton btnSearch = new JButton("Search");
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			    String usage =
			    	      "Usage:\tjava org.apache.lucene.demo.SearchFiles [-index dir] [-field f] [-repeat n] [-queries file] [-query string] [-raw] [-paging hitsPerPage]\n\nSee http://lucene.apache.org/core/4_1_0/demo/ for details.";
			    
			    	    if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
			    	      System.out.println(usage);
			    	      System.exit(0);
			    	    }

			    	    String index = "index";
			    	    String field = "contents";
			    	    String queries = null;
			    	    int repeat = 0;
			    	    boolean raw = false;
			    	    String queryString = null;
			    	    int hitsPerPage = 10;
	/////////////////////////////////////////////////////////////////////////////		    	    
			    	    queryString = textField.getText();
			    	    
			    	   
			    	    
			    	    IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
			    	    IndexSearcher searcher = new IndexSearcher(reader);
			    	    Analyzer analyzer = new StandardAnalyzer();

			    	    BufferedReader in = null;
			    	    if (queries != null) {
			    	      in = Files.newBufferedReader(Paths.get(queries), StandardCharsets.UTF_8);
			    	    } else {
			    	      in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
			    	    }
			    	    QueryParser parser = new QueryParser(field, analyzer);
			    	    while (true) {
			    	      if (queries == null && queryString == null) {                        // prompt the user
			    	        System.out.println("Enter query: ");
			    	      }

			    	      String line = queryString != null ? queryString : in.readLine();

			    	      if (line == null || line.length() == -1) {
			    	        break;
			    	      }

			    	      line = line.trim();
			    	      if (line.length() == 0) {
			    	        break;
			    	      }
			    	      
			    	      Query query = parser.parse(line);
			    	      System.out.println("Searching for: " + query.toString(field));
			    	            
			    	      if (repeat > 0) {                           // repeat & time as benchmark
			    	        Date start = new Date();
			    	        for (int i = 0; i < repeat; i++) {
			    	          searcher.search(query, 100);
			    	        }
			    	        Date end = new Date();
			    	        System.out.println("Time: "+(end.getTime()-start.getTime())+"ms");
			    	      }

			    	      doPagingSearch(in, searcher, query, hitsPerPage, raw, queries == null && queryString == null);

			    	      if (queryString != null) {
			    	        break;
			    	      }
			    	    }
			    	    reader.close();
			}
		});
		btnSearch.setFont(new Font("Arial Black", Font.BOLD, 18));
		btnSearch.setBackground(UIManager.getColor("Button.shadow"));
		btnSearch.setBounds(178, 70, 113, 33);
		contentPane.add(btnSearch);
		
		JTextArea textArea = new JTextArea();
		textArea.setBounds(128, 114, 224, 125);
		contentPane.add(textArea);
	}

}
