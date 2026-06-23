# Basics: Methods

## Task

Create a small method that classifies an integer by its sign.

Implement `NumberClassifier.classify(int number)` so it returns:

- `positive` when the input is greater than zero
- `negative` when the input is less than zero
- `zero` when the input is zero

## Example

```java
NumberClassifier.classify(-3); // "negative"
NumberClassifier.classify(0);  // "zero"
NumberClassifier.classify(8);  // "positive"
```

## Run the tests

From the repository root:

```bash
mvn -pl exercises/basics-methods -am test
```

<details>
<summary>Reference approach</summary>

Use a `static` method because the exercise is about practicing method inputs,
branching, and return values without needing to create an object.

One straightforward solution is:

1. Check whether the number is greater than zero and return `positive`.
2. Check whether the number is less than zero and return `negative`.
3. If neither condition matches, return `zero`.

</details>
