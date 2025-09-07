import argparse
from fit_cli.commands import init_cmd

def main():
    parser = argparse.ArgumentParser(description="FIT Framework CLI 插件开发工具")
    subparsers = parser.add_subparsers(dest="command")

    # init
    parser_init = subparsers.add_parser("init", help="生成插件模板")
    parser_init.add_argument("name", help="插件目录名称")
    parser_init.set_defaults(func=init_cmd.run)

    args = parser.parse_args()

    if hasattr(args, "func"):
        args.func(args)
    else:
        parser.print_help()


if __name__ == "__main__":
    main()
