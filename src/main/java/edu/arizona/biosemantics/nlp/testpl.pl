use SentenceSpliter;

#$P = q/[\.!?;:]/;			## PUNCTUATION
#$AP = q/(?:'|"|»|\)|\]|\})?/;	## AFTER PUNCTUATION

@sentences = SentenceSpliter::get_sentences($text);#@todo: avoid splits in brackets. how?
foreach (@sentences){
 print $_."\n";
}