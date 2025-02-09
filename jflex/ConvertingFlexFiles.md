To convert `.flex` files from Java to Kotlin, you only need to change the code in three sections:
1. The `Usercode` section: this is everything until the first `%%` symbol. This usually only contains the package definition and imports. **Every java STL import should be replaced with a Kotlin equivalent**. For instance, since emitted Kotlin lexers use `kotlinx` for file IO, the following three imports must be added:
    ```kotlin
    import kotlinx.io.*
    import kotlinx.io.files.SystemFileSystem
    import kotlinx.io.files.Path
    ```

2. Code between `%{ ... %}` in the `Options and Macros` section, between the first and last `%%` symbols: this segment usually contains constructors / methods. Nothing special has to be done here besides converting the code to Kotlin.

3. Code in the `Rules and Actions` section: this is everything after the final `%%` symbol. All of the actions for different states are Java code, which has to be converted to Kotlin. As before, there is nothing special that needs to be done besides.