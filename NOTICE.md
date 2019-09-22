# Legal 

## Changes

This software is derived ("forked") from the code base in
[`AugeoSoftware/VJDBC` GitHub project][1], which itself was derived from the
[`VJDBC` SourceForge project][2] originally developed by Michael Link and
associates.

While the core concept and a large part of the original VJDBC implementation
has been retained, significant parts of the existing code base has been
extended and re-written, with a number of new engineering challenges being
addressed. The network protocol used, the configuration files, and JDBC URL
format have been changed just to name a few.

The resulting product is no longer compatible with the original
VJDBC software: neither network communication is possible, nor configuration
files and JDBC URLs can be used across the two products. The new products is
called "Relay JDBC" with package structure, configuration file structure,
JDBC URL prefix etc. reflecting the new name.

## Original authors and credits

Individual source file header(s) referencing the original VJDBC product
and its developers are no longer retained individually without the intent
of claiming credits of the work done by the original developer. Instead,
we consider Git SCM change logs as the best way to identify the creator of
certain code sections. All original code sections are respectfully credited
to the original VJDBC project and developer, Michael Link. For all code
sections, where there is other Git SCM log indication of changes are to be
attributed according to the original source file notice:
```
VJDBC - Virtual JDBC
Written by Michael Link
Website: http://vjdbc.sourceforge.net
```

## Licence

This software is distributed under the terms of the Free Software Foundation
[GNU LESSER GENERAL PUBLIC LICENSE Version 2.1][3].

This product includes software developed by the
[Apache Software Foundation][4].

[1]: https://github.com/AugeoSoftware/VJDBC
[2]: http://vjdbc.sourceforge.net/
[3]: https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
[4]: http://www.apache.org/

