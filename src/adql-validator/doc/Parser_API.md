# Parser API

In order to use it inside this validator, an ADQL parser MUST follow some
constraints:

- it must run in command line
- it must support a given set of arguments
- it must return a normalized document

## Input arguments

| Argument (short) | Argument (long) | Mandatory? | Description                                                          |
|------------------|-----------------|------------|----------------------------------------------------------------------|
| `-v`             | --version       | false      | ADQL version of the given ADQL query. Accepted values: `2.0`, `2.1`. |
| QUERY            |                 | true       | ADQL query to run                                                    |

Example:

```bash
myADQLParser -v 2.0 'SELECT * FROM myTable'
```

## Output content

In case of success, the parser MUST return an exit code of `0`. In case of
error, the exit code MUST be different from 0. Currently, exit codes are not
standardized.

The error output is used only when the parser failed for an unexpected reason.
This is considered as a FATAL error.

In the standard output, a JSON document MUST be returned. It MUST contain the
following fields:

| Field      | Type    | Description                                                                                                |
|------------|---------|------------------------------------------------------------------------------------------------------------|
| `success`  | boolean | `true` or `false`                                                                                          |
| `error`    | string  | When `"success"=true`, this field is expected. It must give a human error message explaining the failure.  |
| `duration` | integer | Parsing duration in ms.                                                                                    |

Example in case of success:

```json
{
  "success" : true,
  "duration": 23
}
```

Example in case of error:

```json
{
  "success" : false,
  "error"   : "Encountered 'size' at line 1, column 25. 'size' is a reserved SQL keyword.",
  "duration": 12
}
```