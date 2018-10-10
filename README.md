
# License Adder

### Overview
The license adder is a program that will evaluate the repositories of a Github organization
and create pull requests for any repository that does not contain a license. The default license
to be added is the GNU Public License, the license this project is licensed under.

### Setup
Java 1.8 must be installed and on the path prior to running this program. If you desire to build it from source, Maven must also be installed. The project can be built by calling `mvn clean package` and the 
project jar will be created in the `target` directory. The completed jar has been provided in this repository for convenience and can be found at `license-adder.jar`.

### Usage
```bash
    -b, --base <name> (default: master)
    -c, --credentials <path> (optional)
    -l, --license <path> (default: this project's license)
    -n, --new-branch <name> (default: license)
    -o, --organization <name> (required)
    -p, --password <password> (required if not using -c)
    -u, --username <username> (required if not using -c)
```

`base` - the base branch that the license branch will branch from
`credentials` - the path to a credentials file. This file should have the user's Github username on the
first line, and their password on the second line. If this option is used, `username` and `password`
should not be used
`license` - the path to a license file
`new-branch` - the name of the new branch that will be used for adding the license
`organization` - the name of the Github organization to check
`password` - the password for the user's Github account
`username` - the user's Github username

Note: The credentials file will be preferred over the `username` and `password` flags.


Assuming you are in a Bash terminal in this project's root directory with the jar file in the same directory, the program could be run as follows:

`java -jar license-adder.jar -o My-Organization -c /path/to/credentials`

or

`java -jar license-adder.jar -l /path/to/license -b my-branch -n my-license-branch -o My-Organization -u someuser -p SuperSecret`
