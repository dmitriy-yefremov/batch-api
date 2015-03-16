# Facebook-style batch API for Play

This project contains an example of a Facebook-style batch API implementation for Play. For more details please see
[this blog post](http://yefremov.net/blog/play-batch-api/).

To get started with the project:

```
play run

curl http://localhost:9000/foo
curl http://localhost:9000/bar

curl "http://localhost:9000/batch?f=%2Ffoo&b=%2Fbar"
```
