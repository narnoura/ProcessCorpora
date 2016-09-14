/**
 * Created by noura on 8/30/16.
 */

/*
run:
nohup java -Xms20g -Xmx20g -cp . ProcessArabicCorpora
madafile=../../../../word2vec/data/madamira/arwiki-20160820-pages-articles.txt.mada
-lemmatize
>& buildlemmas.out &
*/

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import data.Token;

public class ProcessArabicCorpora {

    public static final HashMap<String,Boolean> process_options;

    static  {
        process_options = new HashMap<String,Boolean>();
        process_options.put("convertBW", false);
        process_options.put("lemmatize", true);
        //process_options.put("pos", false);
        process_options.put("tok", false);
    }

    public static void main (String[] args)  {
        // This is a utf8 file, tokenized in whatever way
        String file = "";
        // .mada file containing morphological analysis
        String mada_file = "";
        for (int i=0; i< args.length; i++) {
            if (args[i].startsWith("tokfile=")) {
                file = args[i].substring("tokfile=".length());
            }
            else if (args[i].startsWith("madafile=")) {
                mada_file = args[i].substring("madafile=".length());
            }
            else if (args[i].startsWith("-romanize")) {
                process_options.put("convertBW", true);
            }
            else if (args[i].startsWith("-lemmatize")) {
                process_options.put("lemmatize", true);
            }
            else if (args[i].startsWith("-tokenize")) {
                process_options.put("tok", true);
            }
           /* else if (args[i].startsWith("-pos")) {
                process_options.put("pos", true);
            }*/
        }
        String output = file + ".bw.processed";
        ProcessArabicCorpora ArabicProcessor = new ProcessArabicCorpora();
        boolean skipempty = true;
        System.out.println("Reading corpus data");
        List<String> lines = util.FileReader.ReadFile(file,"utf8ar",skipempty);
        List<String> output_lines = new ArrayList<String>();
        // Romanize (use when reading the tokenized file or any UTF8 file)
        if (process_options.get("convertBW")) {
            System.out.println("Converting to Buckwalter");
            output_lines = ArabicProcessor.ConvertToBuckWalter(lines);
        }
        // Lemmatize using Madamira processed output (use when reading the mada file)
        if (process_options.get("lemmatize")) {
            output = mada_file + ".lemmas.processed";
            System.out.println("Reading Madamira output");

            // Read mada file
            //List<List<Token>> mada_tokens = ReadMadaFile(mada_file, skipempty);

            if (process_options.get("tok")) {
                output = file + "tok-lemmas.processed";
                List<String> mada_lines = ReadMadaLemmas(mada_file,skipempty);
                String output_line = "";
                System.out.println("Adding tokens");
                for (int i=0; i<lines.size(); i++) {
                    System.out.println(i);
                    String line = lines.get(i);
                    String mada_line = mada_lines.get(i);
                    String[] words = line.split(" ");
                    String[] lemmas = mada_line.split(" ");
                    int lem = 0;
                    for (String word: words ) {
                        // collect extra tokens
                        if (word.contains("+")) {
                            output_line += word + " ";
                        } else {
                            output_line += lemmas[lem] + " ";
                            lem += 1;
                        }
                    }
                    output_line = output_line.trim();
                    output_lines.add(output_line);
                    output_line = "";
                }

            } else{
                output_lines = ReadMadaLemmas(mada_file,skipempty);
            }

            // If extra tokens need to be added to the output file
           /* if (process_options.get("tok")) {
                System.out.println("Reading tokenized output");
                lines = ArabicProcessor.GetTokenizedOutput(mada_tokens,lines,
                        process_options.get("lemmatize"),process_options.get("pos"));
            }
            // Otherwise, just read and output the lemmas/pos
            else {
                System.out.println("Reading lemmatized output");
                lines = ArabicProcessor.GetOutput(mada_tokens,process_options.get("lemmatize"),
                        process_options.get("pos"));
            }*/
        }
        // Write to file for input to word2vec
        System.out.println("Writing to output");
        ArabicProcessor.WriteToFile(output_lines,output);
        System.out.println("Done writing");
    }


    // If tok, go through tokenized file, print the word, increment counter only when no +, and
    // read corresponding mada token, then output
    public List<String> GetTokenizedOutput(List<List<Token>> tokens, List<String> tok_lines,
                                           boolean lemmatize, boolean pos) {

        List<String> output = new ArrayList<String>();
        for (int i=0;i<tok_lines.size();i++) {
            String output_line = "";
            String tok_line = tok_lines.get(i);
            String[] tokenized = tok_line.split(" ");
            List<Token> mada_tokens = tokens.get(i);
            int notok=0;
            for (String t: tokenized) {
                // t is an extra token
                if (t.contains("+")) {
                    output_line += t;
                    output_line += " ";
                } else {
                    Token this_token = mada_tokens.get(notok);
                    String lemma = "";
                    String partofspeech = "";
                    if (!this_token.morph_features.containsKey("NO_ANALYSIS")) {
                        lemma = this_token.morph_features.get("lex");
                        partofspeech = this_token.morph_features.get("pos");
                    } else {
                        lemma = this_token.morph_features.get("WORD");
                        partofspeech = "NO_ANALYSIS";
                    }
                    if (lemmatize) {
                        output_line += lemma;
                    } else {
                        output_line += this_token.morph_features.get("WORD");
                    }
                    if (pos) {
                        output_line += "/" + partofspeech + " ";
                    } else{
                        output_line += " ";
                    }
                    notok+=1;
                }
                output_line = util.Tokenizer.RemoveExtraWhiteSpace(output_line);
                output.add(output_line);
            }
        }

        return output;
    }

    public List<String> GetOutput(List<List<Token>> tokens, boolean lemmatize, boolean pos) {

        List<String> output = new ArrayList<String>();
        for (int i=0;i<tokens.size();i++) {
            String output_line = "";
            StringBuilder builder = new StringBuilder();
            List<Token> line_tokens = tokens.get(i);
            for (Token t: line_tokens) {
                String lemma = "";
                String partofspeech = "";
                if (!t.morph_features.containsKey("NO_ANALYSIS")) {
                    lemma = t.morph_features.get("lex");
                    partofspeech = t.morph_features.get("pos");
                } else {
                    lemma = t.morph_features.get("WORD");
                    partofspeech = "NO_ANALYSIS";
                }
                if (lemmatize) {
                    builder.append(lemma);
                } else {
                    builder.append(t.morph_features.get("WORD"));
                }
                if (pos) {
                    builder.append( "/" + partofspeech + " ");
                } else{
                    builder.append(" ");
                }
            }
            output_line = builder.toString();
            output_line = util.Tokenizer.RemoveExtraWhiteSpace(output_line);
            output.add(output_line);
        }
        return output;
    }

    public List<String> ConvertToBuckWalter(List<String> lines) {
        for (String line: lines) {
            line = util.BuckwalterConverter.ConvertToBuckwalter(line);
        }
        return lines;
    }

    public void WriteToFile(List<String> lines, String output_path) {
        String out = "";
        try {
            Writer outw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(output_path), "UTF-8"));
            for (String line : lines) {
                outw.write(line + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> ReadMadaLemmas (String file_path, boolean skipempty) {

        System.out.println("Reading mada lemmas");
        //List<String> mada_lines = util.FileReader.ReadFile(file_path,"bw", skipempty);

        List<String> output = new ArrayList<String>();
        String output_line = "";
        String word = "";
        String lemma = "";

        // Process mada line
        try {
            InputStream is = new FileInputStream(file_path);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = "";
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith(";;; SENTENCE ")) {
                   output_line = "";
                }
                else if (line.startsWith(";;WORD ")) {
                    word = line.substring(";;WORD ".length());
                }
                else if (line.startsWith("*")) {
                    String[] morph = line.split(" ");
                    for (int i=1; i< morph.length; i++) {
                        String[] feature=morph[i].split(":",2);
                        if (feature[0].equals("lex")) {
                            lemma = feature[1];
                        }
                    }
                    output_line += lemma + " ";
                }
                else if (line.startsWith("NO-ANALYSIS")) {
                   output_line += word + " ";
                }
                else if (line.startsWith("SENTENCE BREAK")) {
                    output_line = output_line.trim();
                    output.add(output_line);
                }
            } // end for
        } // end try
        catch (Exception e) {
            e.printStackTrace();
        }
        return output;
    }

    public static List<List<Token>> ReadMadaFile (String file_path, boolean skipempty) {

        List<String> mada_lines = util.FileReader.ReadFile(file_path,"bw", skipempty);

        // Process mada line
        List<List<Token>> tokens = new ArrayList<List<Token>>();
        List<Token> these_tokens = new ArrayList<Token>();
        Token this_token = new Token();
        //HashMap<String, String> token_features = new HashMap<String,String>();
        try {
            for (String line: mada_lines) {
                line = line.trim();
                if (line.startsWith(";;; SENTENCE_ID")) {
                    these_tokens = new ArrayList<Token>();
                    //token_features = new HashMap<String,String>();
                }
                else if (line.startsWith(";;; SENTENCE ")) {
                    these_tokens = new ArrayList<Token>();
                    //token_features = new HashMap<String,String>();
                }
                else if (line.startsWith(";;WORD ")) {
                    String word = line.substring(";;WORD ".length());
                    word = util.Tokenizer.RemoveExtraWhiteSpace(word);
                    this_token = new Token(word,"word");
                    //this_token.morph_features.put("WORD",word);
                    //token_features.put("WORD", word);
                }
               /* else if (line.startsWith(";;LENGTH ")) {
                    String length = line.substring(";;LENGTH ".length());
                    length = util.Tokenizer.RemoveExtraWhiteSpace(length);
                    this_token.morph_features.put("LENGTH",length);
                    //token_features.put("LENGTH", length);
                }
                else if (line.startsWith(";;OFFSET ")) {
                    String offset = line.substring(";;OFFSET ".length());
                    offset = util.Tokenizer.RemoveExtraWhiteSpace(offset);
                    this_token.morph_features.put("OFFSET",offset);
                    //token_features.put("OFFSET", offset);
                }
			    else if (line.startsWith(";;SVM_PREDICTIONS ")) {
				//String predictions = line.substring(";;SVM_PREDICTIONS ".length());
			   }*/
                else if (line.startsWith("*")) {
                    String[] morph = line.split(" ");
                    for (int i=1; i< morph.length; i++) {
                        String[] feature=morph[i].split(":",2); //avoid ::f
                        //token_features.put(feature[0], feature[1]);
                        //this_token.morph_features.put(feature[0],feature[1]);
                       if (feature[0].equals("lex")) {
                            this_token.morph_features.put("lex",feature[1]);
                        }
                    }
                    //this_token.SetMorphFeatures(token_features);
                    this_token.SetPOS(this_token.morph_features.get("pos"));
                    if (this_token.text_ != null && !(this_token.text_.equals(""))) {
                        these_tokens.add(this_token);
                    }
                }
                else if (line.startsWith("NO-ANALYSIS")) {
                    this_token.morph_features.put("NO_ANALYSIS", "NO_ANALYSIS");
                    //token_features.put("NO_ANALYSIS", "NO_ANALYSIS");
                    //this_token.SetMorphFeatures(token_features);
                    this_token.SetPOS("NO_ANALYSIS");
                    if (this_token.text_ != null && !(this_token.text_.equals(""))) {
                        these_tokens.add(this_token);
                        }
                }
                else if (line.startsWith("SENTENCE BREAK")) {
                    tokens.add(these_tokens);
                }

            } // end for
        } // end try
        catch (Exception e) {
            e.printStackTrace();
        }

        return tokens;
    }


}
