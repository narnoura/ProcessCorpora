# ProcessCorpora
Process corpora for building word embeddings in different languages

For Arabic:

1) Run ReadArabicWiki.py to create a text file
2) (If tokenization, lemmas, or POS tagging required)
Run Madamira for morphological analysis and tokenization:

java -Xmx6g -Xms6g -XX:NewRatio=3
-jar MADAMIRA-release-20150421-2.1/MADAMIRA-release-20150421-2.1.jar
-rawinput data/ar-wikipedia/arwikisource-20160820-pages-articles.txt
-rawconfig madamira.forcorpora.xml
-rawoutdir word2vec/data/madamira


3) Process as required with ProcessArabicCorpora (Romanize, tokenize, and add lemmas as needed)
4) Run Google's w2vec on resulting corpus
