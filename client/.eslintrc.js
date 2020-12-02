module.exports = {
    env: {
        browser: true,
        node: true,
    },
    'extends': [
        'plugin:react/recommended',
    ],
    parser: '@typescript-eslint/parser',
    parserOptions: {
        'project': 'tsconfig.json',
        'sourceType': 'module',
    },
    plugins: [
        'eslint-plugin-jsdoc',
        'eslint-plugin-no-null',
        'eslint-plugin-import',
        'eslint-plugin-react',
        'eslint-plugin-react-hooks',
        '@typescript-eslint',
        '@typescript-eslint/tslint',
    ],
    settings: {
        'react': {
            version: 'detect',
        },
    },
    rules: {
        '@typescript-eslint/adjacent-overload-signatures': 'error',
        '@typescript-eslint/array-type': [
            'error',
            {
                'default': 'array',
            },
        ],
        '@typescript-eslint/ban-types': [
            'error',
            {
                'types': {
                    'Object': {
                        'message': 'Avoid using the `Object` type. Did you mean `object`?',
                    },
                    'Function': {
                        'message': 'Avoid using the `Function` type. Prefer a specific function type, like `() => void`, or use `ts.AnyFunction`.',
                    },
                    'Boolean': {
                        'message': 'Avoid using the `Boolean` type. Did you mean `boolean`?',
                    },
                    'Number': {
                        'message': 'Avoid using the `Number` type. Did you mean `number`?',
                    },
                    'String': {
                        'message': 'Avoid using the `String` type. Did you mean `string`?',
                    },
                },
            },
        ],
        '@typescript-eslint/consistent-type-assertions': 'off',
        '@typescript-eslint/consistent-type-definitions': 'error',
        '@typescript-eslint/dot-notation': 'error',
        '@typescript-eslint/explicit-member-accessibility': [
            'off',
            {
                'accessibility': 'explicit',
            },
        ],
        '@typescript-eslint/indent': 'error',
        '@typescript-eslint/member-delimiter-style': [
            'error',
            {
                'multiline': {
                    'delimiter': 'semi',
                    'requireLast': true,
                },
                'singleline': {
                    'delimiter': 'semi',
                    'requireLast': false,
                },
            },
        ],
        '@typescript-eslint/member-ordering': 'off',
        '@typescript-eslint/naming-convention': [
            'error',
            {
                selector: 'default',
                format: ['camelCase', 'UPPER_CASE', 'PascalCase'],
            },
            {
                selector: 'default',
                modifiers: ['private'],
                format: ['camelCase', 'UPPER_CASE', 'PascalCase'],
                leadingUnderscore: 'require',
            },
            {
                selector: 'property',
                filter: '^__html$',
                format: null,
            },
        ],
        '@typescript-eslint/no-empty-function': 'off',
        '@typescript-eslint/no-empty-interface': 'off',
        '@typescript-eslint/no-explicit-any': 'off',
        '@typescript-eslint/no-inferrable-types': 'error',
        '@typescript-eslint/no-misused-new': 'error',
        '@typescript-eslint/no-namespace': 'off',
        '@typescript-eslint/no-param-reassign': 'off',
        '@typescript-eslint/no-parameter-properties': 'off',
        '@typescript-eslint/no-this-alias': 'error',
        '@typescript-eslint/no-unused-expressions': 'error',
        '@typescript-eslint/no-use-before-define': 'off',
        '@typescript-eslint/no-var-requires': 'off',
        '@typescript-eslint/prefer-for-of': 'error',
        '@typescript-eslint/prefer-function-type': 'error',
        '@typescript-eslint/prefer-namespace-keyword': 'error',
        '@typescript-eslint/quotes': [
            'error',
            'single',
            {
                'avoidEscape': true,
            },
        ],
        '@typescript-eslint/semi': [
            'error',
            'always',
        ],
        '@typescript-eslint/triple-slash-reference': [
            'off',
            {
                'path': 'always',
                'types': 'prefer-import',
                'lib': 'always',
            },
        ],
        '@typescript-eslint/type-annotation-spacing': 'error',
        '@typescript-eslint/unified-signatures': 'error',
        'arrow-body-style': 'off',
        'arrow-parens': [
            'error',
            'always',
        ],
        'brace-style': [
            'error',
            '1tbs',
        ],
        'comma-dangle': 'off',
        'complexity': 'off',
        'constructor-super': 'error',
        'curly': [
            'error',
            'multi-line',
        ],
        'eol-last': 'off',
        'eqeqeq': [
            'error',
            'always',
        ],
        'guard-for-in': 'off',
        'id-blacklist': [
            'error',
            'any',
            'Number',
            'number',
            'String',
            'string',
            'Boolean',
            'boolean',
            'Undefined',
            'undefined',
        ],
        'id-match': 'error',
        'import/no-extraneous-dependencies': [
            'error',
            {
                'devDependencies': false,
            },
        ],
        'import/no-internal-modules': 'off',
        'import/order': 'off',
        'jsdoc/check-alignment': 'error',
        'jsdoc/check-indentation': 'error',
        'jsdoc/newline-after-description': 'error',
        'linebreak-style': [
            'error',
            'unix',
        ],
        'max-classes-per-file': 'off',
        'max-len': 'off',
        'new-parens': 'error',
        'no-bitwise': 'off',
        'no-caller': 'error',
        'no-cond-assign': 'off',
        'no-console': 'off',
        'no-debugger': 'off',
        'no-duplicate-case': 'error',
        'no-duplicate-imports': 'error',
        'no-empty': 'off',
        'no-eval': 'off',
        'no-extra-bind': 'error',
        'no-fallthrough': 'error',
        'no-invalid-this': 'off',
        'no-multiple-empty-lines': 'off',
        'no-new-func': 'error',
        'no-new-wrappers': 'error',
        'no-null/no-null': 'off',
        'no-redeclare': 'error',
        'no-return-await': 'error',
        'no-sequences': 'off',
        'no-shadow': [
            'off',
            {
                'hoist': 'all',
            },
        ],
        'no-sparse-arrays': 'error',
        'no-template-curly-in-string': 'error',
        'no-throw-literal': 'error',
        'no-trailing-spaces': 'error',
        'no-undef-init': 'error',
        'no-unsafe-finally': 'error',
        'no-unused-labels': 'error',
        'no-var': 'error',
        'object-shorthand': 'error',
        'one-var': [
            'off',
            'never',
        ],
        'prefer-const': 'error',
        'prefer-object-spread': 'error',
        'quote-props': [
            'error',
            'consistent-as-needed',
        ],
        'radix': 'off',
        'react/jsx-boolean-value': [
            'error',
            'always',
        ],
        'react/jsx-key': 'error',
        'react/jsx-no-bind': 'error',
        'react/no-string-refs': 'error',
        'react/self-closing-comp': 'error',
        'react-hooks/exhaustive-deps': 'error',
        'space-before-function-paren': 'off',
        'space-in-parens': [
            'error',
            'never',
        ],
        'spaced-comment': [
            'error',
            'always',
            {
                'markers': [
                    '/',
                ],
            },
        ],
        'use-isnan': 'error',
        'valid-typeof': 'off',
        '@typescript-eslint/tslint/config': [
            'error',
            {
                'rules': {
                    'whitespace': [
                        true,
                        'check-branch',
                        'check-decl',
                        'check-operator',
                        'check-separator',
                        'check-type',
                    ],
                },
            },
        ],
    },
};
