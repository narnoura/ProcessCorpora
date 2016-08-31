import sys
import gensim
import codecs
from gensim.corpora import WikiCorpus

SPACE = " "

def main():
    filename = "data/ar-wikipedia/arwikisource-20160820-pages-articles.xml.bz2"
    output = "arabicwiki.txt"
    if (sys.argv >= 2):
        filename = sys.argv[1]
    if (sys.argv == 3):
        output = sys.argv[2]
    oututf8 = codecs.open(output,'w','utf-8')

    print "Initializing corpus"
    arabwiki = WikiCorpus(filename,lemmatize=False,dictionary={})

    print "Reading arabic wiki articles"
    for text in arabwiki.get_texts():
        oututf8.write(SPACE.join(text).decode('utf8') + "\n")












if __name__ == "__main__":
    main()