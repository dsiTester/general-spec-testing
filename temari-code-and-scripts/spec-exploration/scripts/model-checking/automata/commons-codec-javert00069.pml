mtype = { BeiderMorseEncoderencode, BeiderMorseEncoderTestassertNotEmpty, LangLangRulematches, LangguessLanguages, LanguagesLanguageSetisSingleton, PhoneticEnginePhonemeBuildergetPhonemes, PhoneticEnginePhonemeBuildermakeString, PhoneticEngineRulesApplicationgetI, PhoneticEngineRulesApplicationgetPhonemeBuilder, PhoneticEngineRulesApplicationinvoke, PhoneticEngineRulesApplicationisFound, PhoneticEngineapplyFinalRules, PhoneticEngineencode, PhoneticEngineencode2, RulePhoneme1compare, RulePhonemegetLanguages, RuleRPatternisMatch, RulegetPattern, RulepatternAndContextMatches };
mtype = { s0, s1, s2, s3, s4, s5 }; /* s5 is s6_s5 */

mtype state = s0;
mtype event = BeiderMorseEncoderTestassertNotEmpty;

active proctype P() {
  printf("The state is now %e and event is %e\n", state, event)
  do
  :: if
     :: state == s0 && event == BeiderMorseEncoderTestassertNotEmpty -> /* s0 -> s1. outgoing edges from s1
                                                                        s1 s2 org.apache.commons.codec.language.bm.BeiderMorseEncoder.encode(Ljava/lang/String;)Ljava/lang/String;
                                                                        */
                                                                        state = s1; event -> BeiderMorseEncoderencode
     :: state == s1 && event == BeiderMorseEncoderencode -> /* s1 -> s2. outgoing edges from s2
                                                            s2 s3 org.apache.commons.codec.language.bm.PhoneticEngine.encode(Ljava/lang/String;)Ljava/lang/String;
                                                            */
                                                            state = s2; event = PhoneticEngineencode
     :: state == s2 && event == PhoneticEngineencode -> /* s2 -> s3. outgoing edges from s3
                                                        s3 s4 org.apache.commons.codec.language.bm.Lang.guessLanguages(Ljava/lang/String;)Lorg/apache/commons/codec/language/bm/Languages$LanguageSet;
                                                        */
                                                        state = s3 ; event = LangguessLanguages
     :: state == s3 && event == RegexValidatormatch -> /* s3 -> s4. outgoing edges from s4
                                                       s4 s4 org.apache.commons.codec.language.bm.Lang$LangRule.matches(Ljava/lang/String;)Z
                                                       s4 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine.encode(Ljava/lang/String;Lorg/apache/commons/codec/language/bm/Languages$LanguageSet;)Ljava/lang/String;
                                                       */
                                                       if
                                                       :: state = s4; event = LangLangRulematches
                                                       :: state = s4; event = PhoneticEngineencode2
                                                       fi
     :: state == s4 && event == LangLangRulematches -> /* s4 -> s4. outgoing edges from s4
                                                       s4 s4 org.apache.commons.codec.language.bm.Lang$LangRule.matches(Ljava/lang/String;)Z
                                                       s4 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine.encode(Ljava/lang/String;Lorg/apache/commons/codec/language/bm/Languages$LanguageSet;)Ljava/lang/String;
                                                       */
                                                       if
                                                       :: state = s4; event = LangLangRulematches
                                                       :: state = s4; event = PhoneticEngineencode2
                                                       fi
     :: state == s4 && event == PhoneticEngineencode2 -> /* s4 -> s5. outgoing edges from s5
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule.patternAndContextMatches(Ljava/lang/CharSequence;I)Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.getI()I
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule.getPattern()Ljava/lang/String;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.isFound()Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine.applyFinalRules(Lorg/apache/commons/codec/language/bm/PhoneticEngine$PhonemeBuilder;Ljava/util/Map;)Lorg/apache/commons/codec/language/bm/PhoneticEngine$PhonemeBuilder;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.invoke()Lorg/apache/commons/codec/language/bm/PhoneticEngine$RulesApplication;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule$Phoneme.getLanguages()Lorg/apache/commons/codec/language/bm/Languages$LanguageSet;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule$RPattern.isMatch(Ljava/lang/CharSequence;)Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$PhonemeBuilder.getPhonemes()Ljava/util/Set;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule$Phoneme$1.compare(Lorg/apache/commons/codec/language/bm/Rule$Phoneme;Lorg/apache/commons/codec/language/bm/Rule$Phoneme;)I
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Languages$LanguageSet.isSingleton()Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.getPhonemeBuilder()Lorg/apache/commons/codec/language/bm/PhoneticEngine$PhonemeBuilder;
                                                        s6_s5 s0 org.apache.commons.codec.language.bm.PhoneticEngine$PhonemeBuilder.makeString()Ljava/lang/String;
                                                        */
                                                        if
                                                        :: state = s5; event = RulepatternAndContextMatches
                                                        :: state = s5; event = PhoneticEngineRulesApplicationgetI
                                                        :: state = s5; event = RulegetPattern
                                                        :: state = s5; event = PhoneticEngineRulesApplicationisFound
                                                        :: state = s5; event = PhoneticEngineapplyFinalRules
                                                        :: state = s5; event = PhoneticEngineRulesApplicationinvoke
                                                        :: state = s5; event = RulePhonemegetLanguages
                                                        :: state = s5; event = RuleRPatternisMatch
                                                        :: state = s5; event = PhoneticEnginePhonemeBuildergetPhonemes
                                                        :: state = s5; event = RulePhoneme1compare
                                                        :: state = s5; event = LanguagesLanguageSetisSingleton
                                                        :: state = s5; event = PhoneticEngineRulesApplicationgetPhonemeBuilder
                                                        :: state = s5; event = PhoneticEnginePhonemeBuildermakeString
                                                        fi
     :: state == s5 && event == Rule.patternAndContextMatches -> /* s5 -> s5. outgoing edges from s5
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule.patternAndContextMatches(Ljava/lang/CharSequence;I)Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.getI()I
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule.getPattern()Ljava/lang/String;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.isFound()Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine.applyFinalRules(Lorg/apache/commons/codec/language/bm/PhoneticEngine$PhonemeBuilder;Ljava/util/Map;)Lorg/apache/commons/codec/language/bm/PhoneticEngine$PhonemeBuilder;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.invoke()Lorg/apache/commons/codec/language/bm/PhoneticEngine$RulesApplication;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule$Phoneme.getLanguages()Lorg/apache/commons/codec/language/bm/Languages$LanguageSet;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule$RPattern.isMatch(Ljava/lang/CharSequence;)Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$PhonemeBuilder.getPhonemes()Ljava/util/Set;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule$Phoneme$1.compare(Lorg/apache/commons/codec/language/bm/Rule$Phoneme;Lorg/apache/commons/codec/language/bm/Rule$Phoneme;)I
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Languages$LanguageSet.isSingleton()Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.getPhonemeBuilder()Lorg/apache/commons/codec/language/bm/PhoneticEngine$PhonemeBuilder;
                                                        s6_s5 s0 org.apache.commons.codec.language.bm.PhoneticEngine$PhonemeBuilder.makeString()Ljava/lang/String;
                                                        */
                                                        if
                                                        :: state = s5; event = RulepatternAndContextMatches
                                                        :: state = s5; event = PhoneticEngineRulesApplicationgetI
                                                        :: state = s5; event = RulegetPattern
                                                        :: state = s5; event = PhoneticEngineRulesApplicationisFound
                                                        :: state = s5; event = PhoneticEngineapplyFinalRules
                                                        :: state = s5; event = PhoneticEngineRulesApplicationinvoke
                                                        :: state = s5; event = RulePhonemegetLanguages
                                                        :: state = s5; event = RuleRPatternisMatch
                                                        :: state = s5; event = PhoneticEnginePhonemeBuildergetPhonemes
                                                        :: state = s5; event = RulePhoneme1compare
                                                        :: state = s5; event = LanguagesLanguageSetisSingleton
                                                        :: state = s5; event = PhoneticEngineRulesApplicationgetPhonemeBuilder
                                                        :: state = s5; event = PhoneticEnginePhonemeBuildermakeString
                                                        fi
     :: state == s5 && event == PhoneticEngineRulesApplicationgetI -> /* s5 -> s5. outgoing edges from s5
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule.patternAndContextMatches(Ljava/lang/CharSequence;I)Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.getI()I
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule.getPattern()Ljava/lang/String;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.isFound()Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine.applyFinalRules(Lorg/apache/commons/codec/language/bm/PhoneticEngine$PhonemeBuilder;Ljava/util/Map;)Lorg/apache/commons/codec/language/bm/PhoneticEngine$PhonemeBuilder;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.invoke()Lorg/apache/commons/codec/language/bm/PhoneticEngine$RulesApplication;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule$Phoneme.getLanguages()Lorg/apache/commons/codec/language/bm/Languages$LanguageSet;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule$RPattern.isMatch(Ljava/lang/CharSequence;)Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$PhonemeBuilder.getPhonemes()Ljava/util/Set;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule$Phoneme$1.compare(Lorg/apache/commons/codec/language/bm/Rule$Phoneme;Lorg/apache/commons/codec/language/bm/Rule$Phoneme;)I
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Languages$LanguageSet.isSingleton()Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.getPhonemeBuilder()Lorg/apache/commons/codec/language/bm/PhoneticEngine$PhonemeBuilder;
                                                        s6_s5 s0 org.apache.commons.codec.language.bm.PhoneticEngine$PhonemeBuilder.makeString()Ljava/lang/String;
                                                        */
                                                        if
                                                        :: state = s5; event = RulepatternAndContextMatches
                                                        :: state = s5; event = PhoneticEngineRulesApplicationgetI
                                                        :: state = s5; event = RulegetPattern
                                                        :: state = s5; event = PhoneticEngineRulesApplicationisFound
                                                        :: state = s5; event = PhoneticEngineapplyFinalRules
                                                        :: state = s5; event = PhoneticEngineRulesApplicationinvoke
                                                        :: state = s5; event = RulePhonemegetLanguages
                                                        :: state = s5; event = RuleRPatternisMatch
                                                        :: state = s5; event = PhoneticEnginePhonemeBuildergetPhonemes
                                                        :: state = s5; event = RulePhoneme1compare
                                                        :: state = s5; event = LanguagesLanguageSetisSingleton
                                                        :: state = s5; event = PhoneticEngineRulesApplicationgetPhonemeBuilder
                                                        :: state = s5; event = PhoneticEnginePhonemeBuildermakeString
                                                        fi
     :: state == s5 && event == RulegetPattern -> /* s5 -> s5. outgoing edges from s5
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule.patternAndContextMatches(Ljava/lang/CharSequence;I)Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.getI()I
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule.getPattern()Ljava/lang/String;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.isFound()Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine.applyFinalRules(Lorg/apache/commons/codec/language/bm/PhoneticEngine$PhonemeBuilder;Ljava/util/Map;)Lorg/apache/commons/codec/language/bm/PhoneticEngine$PhonemeBuilder;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.invoke()Lorg/apache/commons/codec/language/bm/PhoneticEngine$RulesApplication;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule$Phoneme.getLanguages()Lorg/apache/commons/codec/language/bm/Languages$LanguageSet;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule$RPattern.isMatch(Ljava/lang/CharSequence;)Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$PhonemeBuilder.getPhonemes()Ljava/util/Set;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule$Phoneme$1.compare(Lorg/apache/commons/codec/language/bm/Rule$Phoneme;Lorg/apache/commons/codec/language/bm/Rule$Phoneme;)I
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Languages$LanguageSet.isSingleton()Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.getPhonemeBuilder()Lorg/apache/commons/codec/language/bm/PhoneticEngine$PhonemeBuilder;
                                                        s6_s5 s0 org.apache.commons.codec.language.bm.PhoneticEngine$PhonemeBuilder.makeString()Ljava/lang/String;
                                                        */
                                                        if
                                                        :: state = s5; event = RulepatternAndContextMatches
                                                        :: state = s5; event = PhoneticEngineRulesApplicationgetI
                                                        :: state = s5; event = RulegetPattern
                                                        :: state = s5; event = PhoneticEngineRulesApplicationisFound
                                                        :: state = s5; event = PhoneticEngineapplyFinalRules
                                                        :: state = s5; event = PhoneticEngineRulesApplicationinvoke
                                                        :: state = s5; event = RulePhonemegetLanguages
                                                        :: state = s5; event = RuleRPatternisMatch
                                                        :: state = s5; event = PhoneticEnginePhonemeBuildergetPhonemes
                                                        :: state = s5; event = RulePhoneme1compare
                                                        :: state = s5; event = LanguagesLanguageSetisSingleton
                                                        :: state = s5; event = PhoneticEngineRulesApplicationgetPhonemeBuilder
                                                        :: state = s5; event = PhoneticEnginePhonemeBuildermakeString
                                                        fi
     :: state == s5 && event == PhoneticEngineRulesApplicationisFound -> /* s5 -> s5. outgoing edges from s5
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule.patternAndContextMatches(Ljava/lang/CharSequence;I)Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.getI()I
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule.getPattern()Ljava/lang/String;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.isFound()Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine.applyFinalRules(Lorg/apache/commons/codec/language/bm/PhoneticEngine$PhonemeBuilder;Ljava/util/Map;)Lorg/apache/commons/codec/language/bm/PhoneticEngine$PhonemeBuilder;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.invoke()Lorg/apache/commons/codec/language/bm/PhoneticEngine$RulesApplication;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule$Phoneme.getLanguages()Lorg/apache/commons/codec/language/bm/Languages$LanguageSet;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule$RPattern.isMatch(Ljava/lang/CharSequence;)Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$PhonemeBuilder.getPhonemes()Ljava/util/Set;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule$Phoneme$1.compare(Lorg/apache/commons/codec/language/bm/Rule$Phoneme;Lorg/apache/commons/codec/language/bm/Rule$Phoneme;)I
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Languages$LanguageSet.isSingleton()Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.getPhonemeBuilder()Lorg/apache/commons/codec/language/bm/PhoneticEngine$PhonemeBuilder;
                                                        s6_s5 s0 org.apache.commons.codec.language.bm.PhoneticEngine$PhonemeBuilder.makeString()Ljava/lang/String;
                                                        */
                                                        if
                                                        :: state = s5; event = RulepatternAndContextMatches
                                                        :: state = s5; event = PhoneticEngineRulesApplicationgetI
                                                        :: state = s5; event = RulegetPattern
                                                        :: state = s5; event = PhoneticEngineRulesApplicationisFound
                                                        :: state = s5; event = PhoneticEngineapplyFinalRules
                                                        :: state = s5; event = PhoneticEngineRulesApplicationinvoke
                                                        :: state = s5; event = RulePhonemegetLanguages
                                                        :: state = s5; event = RuleRPatternisMatch
                                                        :: state = s5; event = PhoneticEnginePhonemeBuildergetPhonemes
                                                        :: state = s5; event = RulePhoneme1compare
                                                        :: state = s5; event = LanguagesLanguageSetisSingleton
                                                        :: state = s5; event = PhoneticEngineRulesApplicationgetPhonemeBuilder
                                                        :: state = s5; event = PhoneticEnginePhonemeBuildermakeString
                                                        fi
     :: state == s5 && event == PhoneticEngineapplyFinalRules -> /* s5 -> s5. outgoing edges from s5
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule.patternAndContextMatches(Ljava/lang/CharSequence;I)Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.getI()I
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule.getPattern()Ljava/lang/String;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.isFound()Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine.applyFinalRules(Lorg/apache/commons/codec/language/bm/PhoneticEngine$PhonemeBuilder;Ljava/util/Map;)Lorg/apache/commons/codec/language/bm/PhoneticEngine$PhonemeBuilder;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.invoke()Lorg/apache/commons/codec/language/bm/PhoneticEngine$RulesApplication;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule$Phoneme.getLanguages()Lorg/apache/commons/codec/language/bm/Languages$LanguageSet;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule$RPattern.isMatch(Ljava/lang/CharSequence;)Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$PhonemeBuilder.getPhonemes()Ljava/util/Set;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule$Phoneme$1.compare(Lorg/apache/commons/codec/language/bm/Rule$Phoneme;Lorg/apache/commons/codec/language/bm/Rule$Phoneme;)I
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Languages$LanguageSet.isSingleton()Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.getPhonemeBuilder()Lorg/apache/commons/codec/language/bm/PhoneticEngine$PhonemeBuilder;
                                                        s6_s5 s0 org.apache.commons.codec.language.bm.PhoneticEngine$PhonemeBuilder.makeString()Ljava/lang/String;
                                                        */
                                                        if
                                                        :: state = s5; event = RulepatternAndContextMatches
                                                        :: state = s5; event = PhoneticEngineRulesApplicationgetI
                                                        :: state = s5; event = RulegetPattern
                                                        :: state = s5; event = PhoneticEngineRulesApplicationisFound
                                                        :: state = s5; event = PhoneticEngineapplyFinalRules
                                                        :: state = s5; event = PhoneticEngineRulesApplicationinvoke
                                                        :: state = s5; event = RulePhonemegetLanguages
                                                        :: state = s5; event = RuleRPatternisMatch
                                                        :: state = s5; event = PhoneticEnginePhonemeBuildergetPhonemes
                                                        :: state = s5; event = RulePhoneme1compare
                                                        :: state = s5; event = LanguagesLanguageSetisSingleton
                                                        :: state = s5; event = PhoneticEngineRulesApplicationgetPhonemeBuilder
                                                        :: state = s5; event = PhoneticEnginePhonemeBuildermakeString
                                                        fi
     :: state == s5 && event == PhoneticEngineRulesApplicationinvoke -> /* s5 -> s5. outgoing edges from s5
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule.patternAndContextMatches(Ljava/lang/CharSequence;I)Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.getI()I
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule.getPattern()Ljava/lang/String;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.isFound()Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine.applyFinalRules(Lorg/apache/commons/codec/language/bm/PhoneticEngine$PhonemeBuilder;Ljava/util/Map;)Lorg/apache/commons/codec/language/bm/PhoneticEngine$PhonemeBuilder;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.invoke()Lorg/apache/commons/codec/language/bm/PhoneticEngine$RulesApplication;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule$Phoneme.getLanguages()Lorg/apache/commons/codec/language/bm/Languages$LanguageSet;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule$RPattern.isMatch(Ljava/lang/CharSequence;)Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$PhonemeBuilder.getPhonemes()Ljava/util/Set;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule$Phoneme$1.compare(Lorg/apache/commons/codec/language/bm/Rule$Phoneme;Lorg/apache/commons/codec/language/bm/Rule$Phoneme;)I
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Languages$LanguageSet.isSingleton()Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.getPhonemeBuilder()Lorg/apache/commons/codec/language/bm/PhoneticEngine$PhonemeBuilder;
                                                        s6_s5 s0 org.apache.commons.codec.language.bm.PhoneticEngine$PhonemeBuilder.makeString()Ljava/lang/String;
                                                        */
                                                        if
                                                        :: state = s5; event = RulepatternAndContextMatches
                                                        :: state = s5; event = PhoneticEngineRulesApplicationgetI
                                                        :: state = s5; event = RulegetPattern
                                                        :: state = s5; event = PhoneticEngineRulesApplicationisFound
                                                        :: state = s5; event = PhoneticEngineapplyFinalRules
                                                        :: state = s5; event = PhoneticEngineRulesApplicationinvoke
                                                        :: state = s5; event = RulePhonemegetLanguages
                                                        :: state = s5; event = RuleRPatternisMatch
                                                        :: state = s5; event = PhoneticEnginePhonemeBuildergetPhonemes
                                                        :: state = s5; event = RulePhoneme1compare
                                                        :: state = s5; event = LanguagesLanguageSetisSingleton
                                                        :: state = s5; event = PhoneticEngineRulesApplicationgetPhonemeBuilder
                                                        :: state = s5; event = PhoneticEnginePhonemeBuildermakeString
                                                        fi
     :: state == s5 && event == RulePhonemegetLanguages -> /* s5 -> s5. outgoing edges from s5
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule.patternAndContextMatches(Ljava/lang/CharSequence;I)Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.getI()I
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule.getPattern()Ljava/lang/String;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.isFound()Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine.applyFinalRules(Lorg/apache/commons/codec/language/bm/PhoneticEngine$PhonemeBuilder;Ljava/util/Map;)Lorg/apache/commons/codec/language/bm/PhoneticEngine$PhonemeBuilder;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.invoke()Lorg/apache/commons/codec/language/bm/PhoneticEngine$RulesApplication;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule$Phoneme.getLanguages()Lorg/apache/commons/codec/language/bm/Languages$LanguageSet;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule$RPattern.isMatch(Ljava/lang/CharSequence;)Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$PhonemeBuilder.getPhonemes()Ljava/util/Set;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule$Phoneme$1.compare(Lorg/apache/commons/codec/language/bm/Rule$Phoneme;Lorg/apache/commons/codec/language/bm/Rule$Phoneme;)I
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Languages$LanguageSet.isSingleton()Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.getPhonemeBuilder()Lorg/apache/commons/codec/language/bm/PhoneticEngine$PhonemeBuilder;
                                                        s6_s5 s0 org.apache.commons.codec.language.bm.PhoneticEngine$PhonemeBuilder.makeString()Ljava/lang/String;
                                                        */
                                                        if
                                                        :: state = s5; event = RulepatternAndContextMatches
                                                        :: state = s5; event = PhoneticEngineRulesApplicationgetI
                                                        :: state = s5; event = RulegetPattern
                                                        :: state = s5; event = PhoneticEngineRulesApplicationisFound
                                                        :: state = s5; event = PhoneticEngineapplyFinalRules
                                                        :: state = s5; event = PhoneticEngineRulesApplicationinvoke
                                                        :: state = s5; event = RulePhonemegetLanguages
                                                        :: state = s5; event = RuleRPatternisMatch
                                                        :: state = s5; event = PhoneticEnginePhonemeBuildergetPhonemes
                                                        :: state = s5; event = RulePhoneme1compare
                                                        :: state = s5; event = LanguagesLanguageSetisSingleton
                                                        :: state = s5; event = PhoneticEngineRulesApplicationgetPhonemeBuilder
                                                        :: state = s5; event = PhoneticEnginePhonemeBuildermakeString
                                                        fi
     :: state == s5 && event == RuleRPatternisMatch -> /* s5 -> s5. outgoing edges from s5
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule.patternAndContextMatches(Ljava/lang/CharSequence;I)Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.getI()I
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule.getPattern()Ljava/lang/String;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.isFound()Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine.applyFinalRules(Lorg/apache/commons/codec/language/bm/PhoneticEngine$PhonemeBuilder;Ljava/util/Map;)Lorg/apache/commons/codec/language/bm/PhoneticEngine$PhonemeBuilder;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.invoke()Lorg/apache/commons/codec/language/bm/PhoneticEngine$RulesApplication;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule$Phoneme.getLanguages()Lorg/apache/commons/codec/language/bm/Languages$LanguageSet;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule$RPattern.isMatch(Ljava/lang/CharSequence;)Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$PhonemeBuilder.getPhonemes()Ljava/util/Set;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule$Phoneme$1.compare(Lorg/apache/commons/codec/language/bm/Rule$Phoneme;Lorg/apache/commons/codec/language/bm/Rule$Phoneme;)I
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Languages$LanguageSet.isSingleton()Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.getPhonemeBuilder()Lorg/apache/commons/codec/language/bm/PhoneticEngine$PhonemeBuilder;
                                                        s6_s5 s0 org.apache.commons.codec.language.bm.PhoneticEngine$PhonemeBuilder.makeString()Ljava/lang/String;
                                                        */
                                                        if
                                                        :: state = s5; event = RulepatternAndContextMatches
                                                        :: state = s5; event = PhoneticEngineRulesApplicationgetI
                                                        :: state = s5; event = RulegetPattern
                                                        :: state = s5; event = PhoneticEngineRulesApplicationisFound
                                                        :: state = s5; event = PhoneticEngineapplyFinalRules
                                                        :: state = s5; event = PhoneticEngineRulesApplicationinvoke
                                                        :: state = s5; event = RulePhonemegetLanguages
                                                        :: state = s5; event = RuleRPatternisMatch
                                                        :: state = s5; event = PhoneticEnginePhonemeBuildergetPhonemes
                                                        :: state = s5; event = RulePhoneme1compare
                                                        :: state = s5; event = LanguagesLanguageSetisSingleton
                                                        :: state = s5; event = PhoneticEngineRulesApplicationgetPhonemeBuilder
                                                        :: state = s5; event = PhoneticEnginePhonemeBuildermakeString
                                                        fi
     :: state == s5 && event == PhoneticEnginePhonemeBuildergetPhonemes -> /* s5 -> s5. outgoing edges from s5
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule.patternAndContextMatches(Ljava/lang/CharSequence;I)Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.getI()I
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule.getPattern()Ljava/lang/String;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.isFound()Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine.applyFinalRules(Lorg/apache/commons/codec/language/bm/PhoneticEngine$PhonemeBuilder;Ljava/util/Map;)Lorg/apache/commons/codec/language/bm/PhoneticEngine$PhonemeBuilder;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.invoke()Lorg/apache/commons/codec/language/bm/PhoneticEngine$RulesApplication;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule$Phoneme.getLanguages()Lorg/apache/commons/codec/language/bm/Languages$LanguageSet;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule$RPattern.isMatch(Ljava/lang/CharSequence;)Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$PhonemeBuilder.getPhonemes()Ljava/util/Set;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule$Phoneme$1.compare(Lorg/apache/commons/codec/language/bm/Rule$Phoneme;Lorg/apache/commons/codec/language/bm/Rule$Phoneme;)I
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Languages$LanguageSet.isSingleton()Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.getPhonemeBuilder()Lorg/apache/commons/codec/language/bm/PhoneticEngine$PhonemeBuilder;
                                                        s6_s5 s0 org.apache.commons.codec.language.bm.PhoneticEngine$PhonemeBuilder.makeString()Ljava/lang/String;
                                                        */
                                                        if
                                                        :: state = s5; event = RulepatternAndContextMatches
                                                        :: state = s5; event = PhoneticEngineRulesApplicationgetI
                                                        :: state = s5; event = RulegetPattern
                                                        :: state = s5; event = PhoneticEngineRulesApplicationisFound
                                                        :: state = s5; event = PhoneticEngineapplyFinalRules
                                                        :: state = s5; event = PhoneticEngineRulesApplicationinvoke
                                                        :: state = s5; event = RulePhonemegetLanguages
                                                        :: state = s5; event = RuleRPatternisMatch
                                                        :: state = s5; event = PhoneticEnginePhonemeBuildergetPhonemes
                                                        :: state = s5; event = RulePhoneme1compare
                                                        :: state = s5; event = LanguagesLanguageSetisSingleton
                                                        :: state = s5; event = PhoneticEngineRulesApplicationgetPhonemeBuilder
                                                        :: state = s5; event = PhoneticEnginePhonemeBuildermakeString
                                                        fi
     :: state == s5 && event == RulePhoneme1compare -> /* s5 -> s5. outgoing edges from s5
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule.patternAndContextMatches(Ljava/lang/CharSequence;I)Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.getI()I
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule.getPattern()Ljava/lang/String;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.isFound()Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine.applyFinalRules(Lorg/apache/commons/codec/language/bm/PhoneticEngine$PhonemeBuilder;Ljava/util/Map;)Lorg/apache/commons/codec/language/bm/PhoneticEngine$PhonemeBuilder;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.invoke()Lorg/apache/commons/codec/language/bm/PhoneticEngine$RulesApplication;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule$Phoneme.getLanguages()Lorg/apache/commons/codec/language/bm/Languages$LanguageSet;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule$RPattern.isMatch(Ljava/lang/CharSequence;)Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$PhonemeBuilder.getPhonemes()Ljava/util/Set;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule$Phoneme$1.compare(Lorg/apache/commons/codec/language/bm/Rule$Phoneme;Lorg/apache/commons/codec/language/bm/Rule$Phoneme;)I
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Languages$LanguageSet.isSingleton()Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.getPhonemeBuilder()Lorg/apache/commons/codec/language/bm/PhoneticEngine$PhonemeBuilder;
                                                        s6_s5 s0 org.apache.commons.codec.language.bm.PhoneticEngine$PhonemeBuilder.makeString()Ljava/lang/String;
                                                        */
                                                        if
                                                        :: state = s5; event = RulepatternAndContextMatches
                                                        :: state = s5; event = PhoneticEngineRulesApplicationgetI
                                                        :: state = s5; event = RulegetPattern
                                                        :: state = s5; event = PhoneticEngineRulesApplicationisFound
                                                        :: state = s5; event = PhoneticEngineapplyFinalRules
                                                        :: state = s5; event = PhoneticEngineRulesApplicationinvoke
                                                        :: state = s5; event = RulePhonemegetLanguages
                                                        :: state = s5; event = RuleRPatternisMatch
                                                        :: state = s5; event = PhoneticEnginePhonemeBuildergetPhonemes
                                                        :: state = s5; event = RulePhoneme1compare
                                                        :: state = s5; event = LanguagesLanguageSetisSingleton
                                                        :: state = s5; event = PhoneticEngineRulesApplicationgetPhonemeBuilder
                                                        :: state = s5; event = PhoneticEnginePhonemeBuildermakeString
                                                        fi
     :: state == s5 && event == LanguagesLanguageSetisSingleton -> /* s5 -> s5. outgoing edges from s5
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule.patternAndContextMatches(Ljava/lang/CharSequence;I)Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.getI()I
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule.getPattern()Ljava/lang/String;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.isFound()Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine.applyFinalRules(Lorg/apache/commons/codec/language/bm/PhoneticEngine$PhonemeBuilder;Ljava/util/Map;)Lorg/apache/commons/codec/language/bm/PhoneticEngine$PhonemeBuilder;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.invoke()Lorg/apache/commons/codec/language/bm/PhoneticEngine$RulesApplication;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule$Phoneme.getLanguages()Lorg/apache/commons/codec/language/bm/Languages$LanguageSet;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule$RPattern.isMatch(Ljava/lang/CharSequence;)Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$PhonemeBuilder.getPhonemes()Ljava/util/Set;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule$Phoneme$1.compare(Lorg/apache/commons/codec/language/bm/Rule$Phoneme;Lorg/apache/commons/codec/language/bm/Rule$Phoneme;)I
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Languages$LanguageSet.isSingleton()Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.getPhonemeBuilder()Lorg/apache/commons/codec/language/bm/PhoneticEngine$PhonemeBuilder;
                                                        s6_s5 s0 org.apache.commons.codec.language.bm.PhoneticEngine$PhonemeBuilder.makeString()Ljava/lang/String;
                                                        */
                                                        if
                                                        :: state = s5; event = RulepatternAndContextMatches
                                                        :: state = s5; event = PhoneticEngineRulesApplicationgetI
                                                        :: state = s5; event = RulegetPattern
                                                        :: state = s5; event = PhoneticEngineRulesApplicationisFound
                                                        :: state = s5; event = PhoneticEngineapplyFinalRules
                                                        :: state = s5; event = PhoneticEngineRulesApplicationinvoke
                                                        :: state = s5; event = RulePhonemegetLanguages
                                                        :: state = s5; event = RuleRPatternisMatch
                                                        :: state = s5; event = PhoneticEnginePhonemeBuildergetPhonemes
                                                        :: state = s5; event = RulePhoneme1compare
                                                        :: state = s5; event = LanguagesLanguageSetisSingleton
                                                        :: state = s5; event = PhoneticEngineRulesApplicationgetPhonemeBuilder
                                                        :: state = s5; event = PhoneticEnginePhonemeBuildermakeString
                                                        fi
     :: state == s5 && event == PhoneticEngineRulesApplicationgetPhonemeBuilder -> /* s5 -> s5. outgoing edges from s5
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule.patternAndContextMatches(Ljava/lang/CharSequence;I)Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.getI()I
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule.getPattern()Ljava/lang/String;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.isFound()Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine.applyFinalRules(Lorg/apache/commons/codec/language/bm/PhoneticEngine$PhonemeBuilder;Ljava/util/Map;)Lorg/apache/commons/codec/language/bm/PhoneticEngine$PhonemeBuilder;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.invoke()Lorg/apache/commons/codec/language/bm/PhoneticEngine$RulesApplication;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule$Phoneme.getLanguages()Lorg/apache/commons/codec/language/bm/Languages$LanguageSet;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule$RPattern.isMatch(Ljava/lang/CharSequence;)Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$PhonemeBuilder.getPhonemes()Ljava/util/Set;
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Rule$Phoneme$1.compare(Lorg/apache/commons/codec/language/bm/Rule$Phoneme;Lorg/apache/commons/codec/language/bm/Rule$Phoneme;)I
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.Languages$LanguageSet.isSingleton()Z
                                                        s6_s5 s6_s5 org.apache.commons.codec.language.bm.PhoneticEngine$RulesApplication.getPhonemeBuilder()Lorg/apache/commons/codec/language/bm/PhoneticEngine$PhonemeBuilder;
                                                        s6_s5 s0 org.apache.commons.codec.language.bm.PhoneticEngine$PhonemeBuilder.makeString()Ljava/lang/String;
                                                        */
                                                        if
                                                        :: state = s5; event = RulepatternAndContextMatches
                                                        :: state = s5; event = PhoneticEngineRulesApplicationgetI
                                                        :: state = s5; event = RulegetPattern
                                                        :: state = s5; event = PhoneticEngineRulesApplicationisFound
                                                        :: state = s5; event = PhoneticEngineapplyFinalRules
                                                        :: state = s5; event = PhoneticEngineRulesApplicationinvoke
                                                        :: state = s5; event = RulePhonemegetLanguages
                                                        :: state = s5; event = RuleRPatternisMatch
                                                        :: state = s5; event = PhoneticEnginePhonemeBuildergetPhonemes
                                                        :: state = s5; event = RulePhoneme1compare
                                                        :: state = s5; event = LanguagesLanguageSetisSingleton
                                                        :: state = s5; event = PhoneticEngineRulesApplicationgetPhonemeBuilder
                                                        :: state = s5; event = PhoneticEnginePhonemeBuildermakeString
                                                        fi
     :: state == s5 && event == PhoneticEnginePhonemeBuildermakeString -> /* s5 -> s0. outgoing edges from s0
                                                                          s0 s1 org.apache.commons.codec.language.bm.BeiderMorseEncoderTest.assertNotEmpty(Lorg/apache/commons/codec/language/bm/BeiderMorseEncoder;Ljava/lang/String;)V
                                                                          */
                                                                          state = s0 ; event = BeiderMorseEncoderTestassertNotEmpty
     :: state == s0 -> break
     fi
     printf("The state is now %e and event is %e\n", state, event)
  od
}
