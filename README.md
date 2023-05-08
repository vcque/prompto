# Prompto - ChatGPT Coding Assistant

An IntelliJ plugin based on ChatGPT.

## Features

This plugin provides actions that capture your current editor's state to contextualize the prompt you send to ChatGPT. Each action lists the context sent so that you can **be mindful of potential issues with private or sensitive data**.

To use it, fill in your API key in Prompto settings and press Alt-Enter in the editor to be presented with the following Prompto actions:

- `prompto ask`: asks a simple question about the visualized code.
- `prompto implement`: (re-)implements the selected method based on your input (e.g., add documentation or fix this issue).
- `prompto clipboard`: captures your editor's state, builds a Prompto prompt, and saves it to your clipboard so that you can use it in the ChatGPT web app.

## Tips and Use-cases

Here are some tips and use-cases:

- If you don't have an API key, you can still use the Prompto clipboard and paste it in the ChatGPT web app.
- Prompto answers often lack accuracy, and you will have to proof-check them. However, it still provides an amazing starting point to iterate upon.
- Prompto is best used for simple but lengthy tasks such as: writing documentation, unit tests, mock datasets, converter methods, etc.
- Prompto has more knowledge about well-known cases and technologies than niche ones.
- Prompto's attention span is limited and will provide better answers for short or medium files and might outright fail for large ones.

## Feedback

Feedback is welcome at [https://github.com/vcque/prompto](https://github.com/vcque/prompto). Have fun!

## License

This project is licensed under the [MIT License](LICENSE).