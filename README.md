# PaiMC

A custom Minecraft server implementation.

## License

PaiMC is licensed under the GNU General Public License v3.0.
See LICENSE for details.

## How to decompile
java -jar tools/vineflower.jar   --thread-count=1   --verify-merges=1   --verify-pre-post-merges=1   --lambda-to-anonymous-class=1   --included-classes="net/minecraft/core/component/.*"   server/jar/mapped-server-reverse.jar test_decompile
