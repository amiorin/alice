# Alice

Alice is an infrastructure automation tool designed to simplify the provisioning and configuration of a DigitalOcean droplet. It uses a combination of [OpenTofu](https://opentofu.org/), [Ansible](https://www.ansible.com/), and [Clojure/Babashka](https://babashka.org/) to provide a seamless setup experience.

The tool focuses on setting up a Transmission server on a DigitalOcean droplet, including local SSH configuration for easy access.

## Features

- **Infrastructure Provisioning**: Uses OpenTofu to create a DigitalOcean droplet (`s-1vcpu-1gb-35gb-intel`, `ubuntu-25-10-x64`).
- **Remote Configuration**: Employs Ansible to install and configure Transmission and Transmission-daemon.
- **Local Configuration**: Uses Ansible locally to update your `~/.ssh/config` for easy connection to the new host.
- **Workflow Management**: Orchestrated by Clojure and Babashka, leveraging `big-config` for configuration and task execution.

## Prerequisites

- [devenv](https://devenv.sh/): To set up the development environment.
- [DigitalOcean API Token](https://cloud.digitalocean.com/account/api/tokens): Required for OpenTofu to manage resources.
- SSH Key: The OpenTofu configuration expects an SSH key with ID `812184` (you may need to modify this in `resources/io/github/amiorin/alice/tools/tofu/main.tf`).

## Getting Started

### 1. Set up the Environment

Use `devenv` to enter the development shell, which will automatically provide all necessary tools (Babashka, Clojure, OpenTofu, Ansible, etc.):

```bash
direnv allow # if you use direnv
# OR
devenv shell
```

### 2. Configure DigitalOcean Token

Set the `DIGITALOCEAN_TOKEN` environment variable:

```bash
export TF_VAR_do_token="your_digitalocean_token"
```

### 3. Usage

The project uses Babashka (`bb`) to run tasks.

#### Full Workflow: Create Everything

To provision the droplet, configure the server, and update your local SSH config:

```bash
bb alice create
```

#### Individual Tasks

You can also run tasks individually:

- **Tofu Tasks**:
  ```bash
  bb tofu render tofu:init tofu:apply:-auto-approve
  bb tofu render tofu:init tofu:destroy:-auto-approve
  ```

- **Ansible Remote Tasks**:
  ```bash
  bb ansible render ansible-playbook:main.yml
  ```

- **Ansible Local Tasks**:
  ```bash
  bb ansible-local render ansible-playbook:main.yml
  ```

#### Cleanup

To destroy the provisioned infrastructure:

```bash
bb alice delete
```

## Project Structure

- `src/io/github/amiorin/alice/`: Clojure source code for workflow orchestration.
- `resources/io/github/amiorin/alice/tools/`: Configuration templates and scripts.
    - `tofu/`: OpenTofu (Terraform) configuration for DigitalOcean.
    - `ansible/`: Ansible playbook for remote server configuration.
    - `ansible-local/`: Ansible playbook for local machine configuration.
- `deps.edn`: Clojure dependencies.
- `bb.edn`: Babashka task definitions.
- `devenv.nix`: Development environment configuration.

## License

Copyright © 2026 Alberto Miorin

Distributed under the MIT License.
