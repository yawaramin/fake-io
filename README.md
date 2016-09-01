# fake-io

This is a project to demonstrate abstracting out IO effects from Scala
programs to promote testing without mocking. It is inspired by similar
work done in the Haskell community, such as:

  - https://engineering.imvu.com/2015/06/20/testable-io-in-haskell-2/

  - https://blog.pusher.com/unit-testing-io-in-haskell/

There are a few caveats:

  - Of course, the Scala typeclass-and-concept-based code is much more
    cumbersome than the equivalent Haskell code

  - Scala Futures work only as a rough approximation for capturing the
    asynchronous IO side effect, as long as we're careful to call the
    `Future.apply` method in _exactly_ the right places

  - There isn't an exact correspondence between the behaviour of the
    production implementation and the fake implementation, but what we
    are looking for is that the fake implementation mimic the production
    code behaviour for the purposes of the tests which seek to specify
    the overall system behaviour.

