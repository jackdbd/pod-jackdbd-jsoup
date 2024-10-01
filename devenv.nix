{pkgs, ...}: {
  enterShell = ''
    versions
  '';

  enterTest = ''
    echo "Running tests"
    git --version | grep --color=auto "${pkgs.git.version}"
  '';

  env = {
    # Set these environment variables to true if you want a statically linked
    # binary when compiling with GraalVM native image.
    BABASHKA_MUSL = "true";
    BABASHKA_STATIC = "true";

    # https://github.com/babashka/pods?tab=readme-ov-file#where-does-the-pod-come-from
    # BABASHKA_PODS_DIR = "target";
    # BABASHKA_PODS_DIR = "~./babashka/pods";

    # GraalVM's native-image tool often requires environment variables to locate
    # system libraries.
    CPATH = "${pkgs.zlib.dev}/include:${pkgs.glibc.dev}/include";
    LIBRARY_PATH = "${pkgs.zlib}/lib:${pkgs.glibc}/lib";
    NIX_LDFLAGS = "-L${pkgs.zlib}/lib -L${pkgs.glibc}/lib";

    CLOJARS_USERNAME = "jackdbd";

    # In order to access an absolute path we need to set impure: true in devenv.yaml
    CLOJARS_PASSWORD = let
      clojars_file = builtins.readFile "/run/secrets/clojars";
      clojars_secrets = builtins.fromJSON clojars_file;
    in
      clojars_secrets.deploy_token;
  };

  languages = {
    clojure.enable = true;
    nix.enable = true;
  };

  packages = with pkgs; [
    babashka
    git

    # which GraalVM to use?
    graalvmCEPackages.graalvm-ce-musl
    # musl is required by GraalVM native-image when compiling a statically
    # linked executable. I guess I need to include musl if I use graalvm-ce, but
    # not if I use graalvmCEPackages.graalvm-ce-musl.
    # musl
    # graalvm-ce

    neil
    zlib # required by GraalVM native-image
  ];

  pre-commit.hooks = {
    alejandra.enable = true;
    cljfmt.enable = true;
    deadnix.enable = true;
    # shellcheck.enable = true;
    statix.enable = true;
  };

  scripts = {
    versions.exec = ''
      echo "=== Versions ==="
      bb --version
      git --version
      java --version
      native-image --version
      neil --version
      echo "=== === ==="
    '';
  };
}
