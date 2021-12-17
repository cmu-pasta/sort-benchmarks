# Mu2 benchmarks

## Commands

### Commons CLI
TODO: serialization of inputs

### Commons CSV
Run with JQF:
```
mvn jqf:fuzz -Dclass=diff.CommonsCSVTest -Dmethod=fuzzCSVParser
```
Run with Mu2:
```
mvn mu2:diff -Dclass=diff.CommonsCSVTest -Dmethod=testCSVParser \
    -Dincludes=org.apache.commons.csv \
    -DtargetIncludes=org.apache.commons.csv,diff.CommonsCSVTest
```

### Commons Codec

Run with JQF:
```
mvn jqf:fuzz -Dclass=diff.CommonsCodecTest -Dmethod=fuzzEncodeDecode
```
Run with Mu2:
```
mvn mu2:diff -Dclass=diff.CommonsCodecTest -Dmethod=testEncodeDecode \
    -Dincludes=org.apache.commons.codec.binary.Base64,org.apache.commons.codec.binary.Base64 \
    -DtargetIncludes=diff.CommonsCodecTest,org.apache.commons.codec
```

### Gson

Run with JQF:
```
mvn jqf:fuzz -Dclass=diff.GsonTest -Dmethod=fuzzJSONParser
```
Run with Mu2:
```
mvn mu2:diff -Dclass=diff.GsonTest -Dmethod=testJSONParser \
    -Dincludes=com.google.gson.stream,com.google.gson.Gson,com.google.gson.Json \
    -DtargetIncludes=diff.GsonTest,com.google.gson
```
