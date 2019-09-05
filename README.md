# Clojure.spec + devcards

## Rationale

If you annotate with clojure.spec your UI and your app state you can use test.check generators to create random data that conforms to that spec.

With devcards you can mount multiple versions of your React component to do visual testing. Therefore why not mounting several versions that display your randomly valid data and see how your app behaves?
